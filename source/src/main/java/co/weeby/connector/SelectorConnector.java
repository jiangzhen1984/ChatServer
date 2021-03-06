package co.weeby.connector;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import co.weeby.event.ServerDataAvailableMessage;
import co.weeby.event.UserConnectionMessage;
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
public class SelectorConnector implements Connector {
	
	private static final String TAG ="CONNECTOR";
	
	private Selector   selector;
	private ServerSocketChannel serverChannel;
	private boolean init;
	private State sockStat;
	
	private ListenWorker workder;
	
	private Object lock = new Object();
	
	private Map<SelectionKey, Terminal> terminals;
	
	private ServiceLooper serviceLooper;
	

	public SelectorConnector(ServiceLooper serviceLooper) {
		init = false;
		sockStat = State.UNINIT;
		terminals = new ConcurrentHashMap<SelectionKey, Terminal>();
		this.serviceLooper = serviceLooper;
	}

	public boolean start(Configuration config) {
		if (init) {
			Log.w(TAG, "connector is started ");
			return false;
		}
		synchronized(lock) {
			Log.i(TAG, "starting listener:");
			Log.i(TAG, " address ==> :" + config.address);
			Log.i(TAG, "  port   ==> :" + config.port);
			try {
				selector = Selector.open();
				serverChannel =ServerSocketChannel.open();
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
				serverChannel.close();
			} catch (IOException e1) {
				Log.e(TAG, " serverChannel close failed ", e1);
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
				sockStat = SelectorConnector.State.PREPARING;
				
				try {
					serverChannel.configureBlocking(false);
					serverChannel.bind(new InetSocketAddress(Inet4Address.getByName(config.address), config.port));
					serverChannel.register(selector, SelectionKey.OP_ACCEPT);
				} catch (UnknownHostException e) {
					Log.e(TAG, " server socket bind  failed ", e);
					sockStat = SelectorConnector.State.PREPARED_FAILED;
					lock.notify();
					return;
				} catch (Exception e) {
					Log.e(TAG, " server socket bind  failed ", e);
					sockStat = SelectorConnector.State.PREPARED_FAILED;
					lock.notify();
					return;
				}
			}
			
			
			synchronized (lock) {
				sockStat = SelectorConnector.State.LISTENING;
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
						try {
							Log.i(TAG, "key:" + sk+"  acctp:"+ sk.isAcceptable()+"   read:"+ sk.isReadable()+"  conn:"+ sk.isConnectable()+"  valid:"+ sk.isValid());
							if (sk.isAcceptable()) {
								handleAccept(serverChannel);
								keys.remove();
							} else if (sk.isReadable()) {
								handleDataAvailable(sk);
								keys.remove();
							} else if (sk.isConnectable()) {
								//TODO send client quit
								Log.d(TAG, "client disconnect: ");
								keys.remove();
							}
						} catch (CancelledKeyException e1) {
							Log.d(TAG, "key is cancelled: " + sk , e1);
						}
						
					}
				} catch (Exception e) {
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
		
		
		
		private void handleAccept(ServerSocketChannel serverChannel) throws IOException {
			SocketChannel client = serverChannel.accept();
			client.configureBlocking(false);
			SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
			Terminal clientTerminal = TerminalFactory.constructClientTerminal(client);
			terminals.put(clientKey, clientTerminal);
			clientTerminal.setSelectionKey(clientKey);
			if (serviceLooper != null) {
				//send user connect Message
				serviceLooper.handleMessage(new UserConnectionMessage(clientTerminal, UserConnectionMessage.CONNECT));
			}
		}
		
		private void handleDataAvailable(SelectionKey selectionKey) {
			Terminal terminal = terminals.get(selectionKey);
			if (terminal == null ) {
				Log.e(TAG, "terminal is null for " + selectionKey);
				return;
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
