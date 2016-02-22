package co.weeby.service;

import java.io.IOException;
import java.util.List;

import co.weeby.chat.GlobalCache;
import co.weeby.chat.Room;
import co.weeby.chat.Telnet;
import co.weeby.event.FallbackMessage;
import co.weeby.event.InternalUserRoomMessage;
import co.weeby.event.MessageEvent;
import co.weeby.event.MessageEventType;
import co.weeby.event.UserConnectionMessage;
import co.weeby.event.UserRoomMessage;
import co.weeby.event.UserSendMessage;
import co.weeby.log.Log;
import co.weeby.terminal.ClientTerminal;
import co.weeby.terminal.Terminal;

public class OutputService extends Service {
	
	private static final String TAG = "OutputService";

	public OutputService() {
		// TODO Auto-generated constructor stub
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
		case INTERNAL_USER_ROOM_EVT:
			handleInternalUserRoomMessage((InternalUserRoomMessage) evt);
			break;
		case FALLBACK_EVT:
			handleFallback((FallbackMessage) evt);
			break;
		default:
			break;
		}
		return evt;

	}

	private void handleUserConnect(UserConnectionMessage evt) {
		if (evt.getState() == UserConnectionMessage.CONNECT) {
			writeMessage(evt.getTerminal(), "Welcome to the XYZ chat server! \n Login Name? \n");
		} else {
			Telnet tel = GlobalCache.getInstance().getTel(evt.getTerminal());
			if (tel != null && tel.getRoom() != null) {
				handleUserLeaveRoom(tel);
			}
			
			if (tel != null) {
				evt.setNickName(tel.getNickName());
				GlobalCache.getInstance().removeTel(tel.getTerminal());
				if (tel.getNickName() != null) {
					GlobalCache.getInstance().removeTel(tel.getNickName());
				}
			}
			
			writeMessage(evt.getTerminal(), "BYE \n");
			//Close connect
			evt.getTerminal().close();
		}
	}
	
	
	private void handleFallback(FallbackMessage evt) {
		writeMessage(evt.getTerminal(), evt.getMsg());
	}
	
	
	
	private void handleUserSendMessage(UserSendMessage evt) {
		Telnet tel = GlobalCache.getInstance().getTel(evt.getTerminal());
		if (tel == null) {
			tel = GlobalCache.getInstance().getTel(evt.getMsg());
			if (tel != null) {
				Log.i("OutputService", "Nick name token:" + evt.getMsg());
				writeMessage(evt.getTerminal(), " Sorry, name taken. \n");
				return;
			}
			
			tel = new Telnet((ClientTerminal)evt.getTerminal());
			tel.setNickName(evt.getMsg());
			tel.setLocal(true);
			//Mark this is first time 
			tel.setFirstInit(true);
			GlobalCache.getInstance().saveTel(tel.getNickName(), tel);
			writeMessage(evt.getTerminal(), "Welcome " + tel.getNickName()+" \n");
			return;
		} 
		
		
		if (tel.getRoom() == null) {
			writeMessage(evt.getTerminal(), "You doesn't enter room yet! \n" );
			return;
		}
		
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
	
	
	
	private void handleUserRoom(UserRoomMessage evt) {
		Telnet tel = GlobalCache.getInstance().getTel(evt.getTerminal());
		if (tel == null || tel.getNickName() == null) {
			writeMessage(evt.getTerminal(), "Please input nick name first \n");
			return;
		}
		UserRoomMessage.Action ua = evt.getAc();
		if (ua == UserRoomMessage.Action.ENTER) {
			handleUserEnterRoom(tel, evt.getRoom());
		} else if (ua == UserRoomMessage.Action.LEAVE) {
			handleUserLeaveRoom(tel);
		} else if (ua == UserRoomMessage.Action.LIST) {
			writeMessage(tel.getTerminal(), getRooms());
		}
	}
	
	


	private void handleUserEnterRoom(Telnet tel, String newRoomName) {
		Room room = tel.getRoom();
		if (room != null && newRoomName.equals(room.getName())) {
			writeMessage(tel.getTerminal(), "You have in this room \n ");
			return;
		}
		if (room != null) {
			handleUserLeaveRoom(tel);
		}
		
		
		room = GlobalCache.getInstance().getRoom(newRoomName);
		if (room == null) {
			room = new Room(newRoomName);
			GlobalCache.getInstance().addRoom(newRoomName, room);
		}
		tel.setRoom(room);
		room.addUser(tel);
		
		if (tel.isLocal()) {
			writeMessage(tel.getTerminal(), "entering room: " + room.getName()+" \n ");
			//write user list
			writeMessage(tel.getTerminal(), getUserList(room, tel));
			writeMessage(tel.getTerminal(), " end of list \n");
		}
		writeMessage(tel, room, "* new user joined: " + tel.getNickName() +" \n");
	}
	
	
	
	private void handleUserLeaveRoom(Telnet tel) {
		if (tel.getRoom() == null) {
			writeMessage(tel.getTerminal(), "You doesn't enter room yet ! \n");
		} else {
			writeMessage(tel.getTerminal(), "* user has left chat: " + tel.getNickName()+" (this is you) \n");
			
			writeMessage(tel, tel.getRoom(), "* user has left chat: " + tel.getNickName() +"\n");
			tel.getRoom().removeUser(tel);
			tel.setRoom(null);
		}
	}
	
	
	
	private void handleInternalUserRoomMessage(InternalUserRoomMessage evt) {
		Telnet tel =  GlobalCache.getInstance().getTel(evt.getUserName());
		if (tel == null) {
			Log.e(TAG, " tel is null action:" + evt.getAc());
			return;
		}
		
		if (evt.getAc() == InternalUserRoomMessage.Action.ENTER) {
			handleUserEnterRoom(tel, evt.getRoom());
		} else if (evt.getAc() == InternalUserRoomMessage.Action.LEAVE){
			handleUserLeaveRoom(tel);
		}
	}
	
	
	
	private void writeMessage(Terminal ter, String msg) {
		try {
			byte[] data = msg.getBytes();
			ter.write(data, 0, data.length);
		} catch (IOException e) {
			Log.e("OutputService", "send message error ", e);
		}
	}
	
	private void writeMessage(Telnet tel, Room room, String msg) {
		try {
			byte[] data = msg.getBytes();
			List<Telnet> tls = room.getUsers();
			for (int i = 0; i < tls.size(); i++) {
				Telnet t = tls.get(i);
				if (!t.isLocal()) {
					continue;
				}
				
				if (t !=  tel) {
					t.getTerminal().write(data, 0, data.length);
				}
			}
		} catch (IOException e) {
			Log.e("OutputService", "send message error ", e);
		}
	}
	
	
	private String getRooms() {
		List<Room> rooms = GlobalCache.getInstance().getRooms();
		StringBuffer sb = new StringBuffer();
		sb.append(" Active rooms are: \n");
		for (int i = 0; i < rooms.size(); i++) {
			Room r = rooms.get(i);
			sb.append("  ").append(r.getName()).append(" (").append(r.getUserCount()).append(")\n");
		}
		return sb.toString();
	}
	
	
	private String getUserList(Room room, Telnet tel) {
		StringBuffer sb = new StringBuffer();
		List<Telnet> tls = room.getUsers();
		for (int i = 0; i < tls.size(); i++) {
			Telnet t = tls.get(i);
			sb.append(" * ").append(t.getNickName());
			if (t == tel) {
				sb.append(" (this is you) ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
	
	
	

}
