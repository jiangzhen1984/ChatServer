package co.weeby.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.nio.channels.DatagramChannel;

import junit.framework.TestCase;
import co.weeby.chat.GlobalCache;
import co.weeby.chat.Telnet;
import co.weeby.connector.Configuration;
import co.weeby.event.MessageEvent;
import co.weeby.event.UserConnectionMessage;
import co.weeby.event.UserRoomMessage;
import co.weeby.event.UserSendMessage;
import co.weeby.terminal.ClientTerminal;

public class MessageParserServiceTestCase extends TestCase {
	
	private SyncService service;
	private ClientTerminal terminal;
	private DatagramChannel channel;
	Configuration config;
	private StringBuffer callbackMsg;

	protected void setUp() throws Exception {
		config = new Configuration();
		config.multicastAddress ="234.254.254.254";
		config.multicastPort = 9499;
		config.multicastIface = "wlan1";
		config.useMulicast = true;
		channel = DatagramChannel.open();
		//channel.bind(new InetSocketAddress(config.multicastAddress, config.multicastPort));
		//channel.join(InetAddress.getByName(config.multicastAddress), NetworkInterface.getByName(config.multicastIface));
		service = new SyncService(channel, config);
		terminal = new ClientTerminal((Socket)null);
	}

	public void testService() {
		Telnet tel = new Telnet(terminal);
		tel.setNickName("aaa");
		tel.setLocal(true);
		GlobalCache.getInstance().saveTerminal(terminal, tel);
		
		MessageEvent m1 = new UserSendMessage(terminal);
		tel.setFirstInit(true);
		((UserSendMessage)m1).setMsg("aaa");
		startListener();
		service.service(m1);
		String ret = getCallbackMsg();
		assertEquals("@/online aaa", ret.trim());
		
		
		
		m1 = new UserRoomMessage(terminal, UserRoomMessage.Action.ENTER, "aaa");
		startListener();
		service.service(m1);
		 ret = getCallbackMsg();
		assertEquals("@/join aaa aaa", ret);
		
		m1 = new UserRoomMessage(terminal, UserRoomMessage.Action.LEAVE, "aaa");
		startListener();
		service.service(m1);
		ret = getCallbackMsg();
		assertEquals("@/leave aaa aaa", ret);
		
		
		m1 = new UserRoomMessage(terminal, UserRoomMessage.Action.ENTER, "aaa");
		startListener();
		service.service(m1);
		ret = getCallbackMsg();
		assertEquals("@/join aaa aaa", ret);
		
		
		m1 = new UserSendMessage(terminal);
		((UserSendMessage)m1).setMsg("bbb");
		startListener();
		service.service(m1);
		ret = getCallbackMsg();
		assertEquals("@/send aaa bbb", ret);
		
		m1 = new UserConnectionMessage(terminal, UserConnectionMessage.DISCONNECT);
		startListener();
		service.service(m1);
		ret = getCallbackMsg();
		assertEquals("@/offline aaa", ret.trim());
		
	}
	
	private void startListener() {
		callbackMsg = new StringBuffer();
		new CallbackThread(callbackMsg).start();
		try {
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	private String getCallbackMsg() {

		synchronized (callbackMsg) {
			try {
				callbackMsg.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return callbackMsg.toString();
		
	}
	
	
	
	class CallbackThread extends Thread {
		private StringBuffer callbackMsg;
		
		public CallbackThread(StringBuffer callbackMsg) {
			super();
			this.callbackMsg = callbackMsg;
		}

		@Override
		public void run() {
			try {
				MulticastSocket socket = new MulticastSocket(config.multicastPort);
				socket.joinGroup(InetAddress.getByName(config.multicastAddress));
				byte[] buf = new byte[1024];
				DatagramPacket p = new DatagramPacket(buf, 1024);
				socket.receive(p);
				socket.leaveGroup(InetAddress.getByName(config.multicastAddress));
				socket.close();
				
				synchronized (callbackMsg) {
					callbackMsg.append(new String(p.getData(), 0, p.getLength()));
					callbackMsg.notify();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
//			ByteBuffer buffer = ByteBuffer.allocate(1024);
//			try {
//				channel.receive(buffer);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		
//			
//			synchronized (callbackMsg) {
//				callbackMsg.append(new String(buffer.array()));
//				callbackMsg.notify();
//			}
		}
		
	}
	
	
	
}


