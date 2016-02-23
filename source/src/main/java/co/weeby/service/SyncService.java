package co.weeby.service;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

import co.weeby.chat.GlobalCache;
import co.weeby.chat.Telnet;
import co.weeby.connector.Configuration;
import co.weeby.event.InternalUserSendMessage;
import co.weeby.event.InternalUserStateMessage;
import co.weeby.event.MessageEvent;
import co.weeby.event.MessageEventType;
import co.weeby.event.UserConnectionMessage;
import co.weeby.event.UserRoomMessage;
import co.weeby.event.UserSendMessage;
import co.weeby.log.Log;

public class SyncService extends Service {
	
	private static final String TAG = "SyncService";
	
	private DatagramChannel channel;
	
	private Configuration config;
	

	public SyncService(DatagramChannel channel,  Configuration config) {
		super();
		this.channel = channel;
		this.config = config;
	}




	@Override
	public MessageEvent service(MessageEvent evt) {
		MessageEventType ty = evt.getEvtType();
		switch (ty) {
		case USER_ROOM_EVT:
			handleUserRoom((UserRoomMessage)evt);
			break;
		case CLIENT_SEND_MESSAGE_EVT:
			handleUserSendMessage((UserSendMessage)evt);
			break;
		case CLIENT_CONNECT_EVT:
			handleUserConnect((UserConnectionMessage) evt);
			break;
		case INTERNAL_USER_STATE_EVT:
			handleInternalUserStateMessage((InternalUserStateMessage) evt);
			break;
		case INTERNAL_USER_SEND_MSG_EVT:
			handleInternalUserSendMessage((InternalUserSendMessage) evt);
			
		default:
			break;
		}
		return evt;

	}

	
	
	private void handleUserRoom(UserRoomMessage evt) {
		Telnet tel = GlobalCache.getInstance().getTel(evt.getTerminal());
		if (tel == null || tel.getNickName() == null) {
			return;
		}
		UserRoomMessage.Action ua = evt.getAc();
		if (ua == UserRoomMessage.Action.ENTER) {
			sendSyncMessage(getMessage(SYNC_JOIN_MSG_FG, tel.getNickName(), evt.getRoom()));
		} else if (ua == UserRoomMessage.Action.LEAVE) {
			sendSyncMessage(getMessage(SYNC_LEAVE_MSG_FG, tel.getNickName(), evt.getRoom()));;
		}
	}
	
	
	
	private void handleUserSendMessage(UserSendMessage evt) {
		Telnet tel = GlobalCache.getInstance().getTel(evt.getTerminal());
		if (tel == null) {
			Log.i(TAG, "telnet is null no need sync ");
			return;
		} 
		
		if (tel.isFirstInit()) {
			sendSyncMessage(getMessage(SYNC_ONLINE_MSG_FG, tel.getNickName(), ""));
			tel.setFirstInit(false);
			return;
		}
		
		if (tel.getRoom() == null) {
			Log.i(TAG, "User not in room, no need to sync");
			return;
		}
		sendSyncMessage(getMessage(SYNC_SEND_MSG_FG, tel.getNickName(), evt.getMsg()));
		
	}
	
	
	
	private void handleUserConnect(UserConnectionMessage evt) {
		if (evt.getState() == UserConnectionMessage.DISCONNECT) {
			sendSyncMessage(getMessage(SYNC_OFFLINE_MSG_FG, evt.getNickName(), ""));
		}
	}
	
	
	
	
	
	private void handleInternalUserStateMessage(InternalUserStateMessage evt) {
		if (evt.isOnline()) {
			Telnet cacheTel = GlobalCache.getInstance().getTel(evt.getUserName());
			if (cacheTel != null) {
				Log.e(TAG, "terminal already exist : " + evt.getUserName());
				return;
			}
			Telnet tel = new Telnet(null);
			tel.setNickName(evt.getUserName());
			tel.setLocal(false);
			GlobalCache.getInstance().saveTel(evt.getUserName(), tel);
		} else {
			GlobalCache.getInstance().removeTel(evt.getUserName());
		}
	}
	
	
	
	

	private void handleInternalUserSendMessage(InternalUserSendMessage evt) {
		Telnet tel = GlobalCache.getInstance().getTel(evt.getUserName());
		
		try {
			StringBuffer sb = new StringBuffer();
			byte[] data = null;
			List<Telnet> tls = tel.getRoom().getUsers();
			for (int i = 0; i < tls.size(); i++) {
				sb.delete(0, sb.length());
				Telnet t = tls.get(i);
				if (!t.isLocal()) {
					continue;
				}
				
				if (t !=  tel) {
					sb.append(t.getNickName()).append(": ");
					sb.append(evt.getMsg()).append("\n");
					data = sb.toString().getBytes();
					t.getTerminal().write(data, 0, data.length);
				}
			}
		} catch (IOException e) {
			Log.e("OutputService", "send message error ", e);
		}
		
	}
	
	
	
	private void sendSyncMessage(String msg) {
		Log.i(TAG, "send sync message == > " + msg);
		try {
			channel.send(ByteBuffer.wrap(msg.getBytes()), new InetSocketAddress(
					Inet4Address.getByName(config.multicastAddress),
					config.multicastPort));
//			
//			MulticastSocket socket = new MulticastSocket();
//			byte[] data = msg.getBytes();
//			DatagramPacket p = new DatagramPacket(data, data.length, Inet4Address.getByName(config.multicastAddress), config.multicastPort);
//			socket.send(p);
//			socket.close();
		} catch (UnknownHostException e) {
			Log.e(TAG, " send sync message error ", e);
		} catch (IOException e) {
			Log.e(TAG, " send sync message error ", e);
		}
	}
	
	
	
	private static final int SYNC_SEND_MSG_FG = 0;
	private static final int SYNC_JOIN_MSG_FG = 1;
	private static final int SYNC_LEAVE_MSG_FG = 2;
	private static final int SYNC_ONLINE_MSG_FG = 3;
	private static final int SYNC_OFFLINE_MSG_FG = 4;
	
	private String getMessage(int flag, String username, String extra) {
		return SYNC_MSG_HEADER[flag] + username +" " + extra;
	}
	
	private static final String[] SYNC_MSG_HEADER = {
		"@/send ",
		"@/join ",
		"@/leave ",
		"@/online ",
		"@/offline ",
	};

}
