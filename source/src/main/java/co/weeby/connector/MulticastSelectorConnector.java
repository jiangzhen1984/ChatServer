package co.weeby.connector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import co.weeby.event.ServerDataAvailableMessage;
import co.weeby.event.UserDataAvailableMessage;
import co.weeby.log.Log;
import co.weeby.service.ServiceLooper;
import co.weeby.terminal.ServerTerminal;
import co.weeby.terminal.Terminal;
import co.weeby.terminal.TerminalFactory;
import co.weeby.terminal.TerminalType;

/**
 * Use NIO select to implement connector
 * @author jiangzhen
 *
 */
public class MulticastSelectorConnector implements Connector {
	
	private static final String TAG ="MulticastSelectorConnector";
	
	private Selector   selector;
	private boolean init;
	private State sockStat;
	
	private DatagramChannel multicastChannel;
	
	private ListenWorker workder;
	
	private Object lock = new Object();
	
	private Map<SelectionKey, Terminal> terminals;
	
	private ServiceLooper serviceLooper;
	

	public MulticastSelectorConnector(ServiceLooper serviceLooper) {
		init = false;
		sockStat = State.UNINIT;
		terminals = new ConcurrentHashMap<SelectionKey, Terminal>();
		this.serviceLooper = serviceLooper;
		try {
			multicastChannel = DatagramChannel.open(StandardProtocolFamily.INET);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean start(Configuration config) {
		if (init) {
			Log.w(TAG, "connector is started ");
			return false;
		}
		
		if (multicastChannel == null) {
			Log.w(TAG, "Can not get channel start failed ");
			return false;
		}
		synchronized(lock) {
			Log.i(TAG, "starting listener:");
			if (config.useMulicast) {
				Log.i(TAG, " multicast address ==> " + config.multicastAddress);
				Log.i(TAG, " multicast port ==> " + config.multicastPort);
			} else {
				throw new RuntimeException("Does not support mulitcast ");
			}
			
			try {
				selector = Selector.open();
				sockStat = State.INITED;
			} catch (IOException e) {
				Log.e(TAG, " connector start failed ", e);
			}
			
			
			init = true;
			workder = new ListenWorker(config);
			workder.start();
			
			try {
				lock.wait();
			} catch (InterruptedException e) {
				Log.e(TAG, " worker start failed ", e);
			}
			
		}
		
		if (sockStat !=  State.LISTENING) {
			Log.e(TAG, " worker start failed " + sockStat);
			destroy();
		}
		
		Log.i(TAG, "  ##### start listener successfully #### ");
		return true;
	}

	public void destroy() {
		synchronized(lock) {
			if (!init) {
				Log.e(TAG, " connector is not init yet ");
				return;
			}
			
			workder.requestQuit();
			
			try {
				lock.wait(6000);
			} catch (InterruptedException e2) {
				Log.e(TAG, " waiting for worker thread quit error ");
			}

			try {
				multicastChannel.close();
			} catch (IOException e) {
				Log.e(TAG, " multicastChannel close failed ", e);
			}
			
			
			
			if (selector != null) {
				try {
					selector.close();
				} catch (IOException e) {
					Log.e(TAG, " selector close failed ", e);
				}
			}
			
			sockStat = State.CLOSED;
		}

	}
	
	
	public DatagramChannel getMulicastChannel() {
		return this.multicastChannel;
	}
	
	
	
	private enum State {
		UNINIT, INITED, PREPARING, LISTENING, CLOSING, CLOSED, PREPARED_FAILED;
	}
	
	
	
	class ListenWorker extends Thread {

		private Configuration config;
		
		private boolean stop;
		
		public ListenWorker(Configuration config) {
			super();
			this.config = config;
		}

		@Override
		public void run() {
			synchronized (lock) {
				sockStat = MulticastSelectorConnector.State.PREPARING;
				
				try {
					
					InetAddress group = InetAddress.getByName(config.multicastAddress);
					NetworkInterface ni = NetworkInterface.getByName(config.multicastIface);
					multicastChannel.bind(new InetSocketAddress(config.multicastPort));
					multicastChannel.configureBlocking(false);
					multicastChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
					multicastChannel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
					multicastChannel.join(group, ni);
					
					multicastChannel.register(selector, SelectionKey.OP_READ);
					
					 
				} catch (UnknownHostException e) {
					Log.e(TAG, " server socket bind  failed ", e);
					sockStat = MulticastSelectorConnector.State.PREPARED_FAILED;
					lock.notify();
					return;
				} catch (Exception e) {
					Log.e(TAG, " server socket bind  failed ", e);
					sockStat = MulticastSelectorConnector.State.PREPARED_FAILED;
					lock.notify();
					return;
				}
			}
			
			
			synchronized (lock) {
				sockStat = MulticastSelectorConnector.State.LISTENING;
				lock.notify();
			}

			//FIXME add exception handler avoid dead loop
			while(!stop) {
				try {
					int ret = selector.select();
					Log.w(TAG, "selector return :" + ret);
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					while(keys.hasNext()) {
						SelectionKey sk = keys.next();
						Log.i(TAG, "key:" + sk+"  acctp:"+ sk.isAcceptable()+"   read:"+ sk.isReadable()+"  conn:"+ sk.isConnectable()+"  valid:"+ sk.isValid());
						if (sk.isAcceptable()) {
							Log.e(TAG, "Ilegal state for multicast acceptable");
							keys.remove();
						} else if (sk.isReadable()) {
							handleDataAvailable(sk);
							keys.remove();
						} else if (sk.isConnectable()) {
							Log.d(TAG, "Ilegal state for multicast  connectable" );
							keys.remove();
						}
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			synchronized (lock) {
				lock.notify();
			}
		}
		
		
		public void requestQuit() {
			stop = true;
		}
		

		private void handleDataAvailable(SelectionKey selectionKey) {
			Terminal terminal = terminals.get(selectionKey);
			if (terminal == null ) {
				//server node send data. client channel never come to here
				terminal = TerminalFactory.constructServerNodeTerminal((DatagramChannel)selectionKey.channel());
				terminals.put(selectionKey, terminal);
			}
			
			if (terminal.isDataAvil()) {
				return;
			} else {
				try {
					terminal.setDataAvil(true);
				} catch (IOException e) {
					Log.e(TAG, " set data notification failed ", e);
				}
				if (serviceLooper != null) {
					if (terminal.getTerminalType() == TerminalType.CLIENT) {
						//send data available Message
						serviceLooper.handleMessage(new UserDataAvailableMessage(terminal));
					} else if (terminal.getTerminalType() == TerminalType.SERVER) {
						serviceLooper.handleMessage(new ServerDataAvailableMessage((ServerTerminal)terminal));
					}
				}
			}
			
		}
		
	}

}
