package co.weeby.service;

import java.io.IOException;

import co.weeby.event.FallbackMessage;
import co.weeby.event.InternalUserRoomMessage;
import co.weeby.event.InternalUserSendMessage;
import co.weeby.event.InternalUserStateMessage;
import co.weeby.event.MessageEvent;
import co.weeby.event.MessageEventType;
import co.weeby.event.ServerDataAvailableMessage;
import co.weeby.event.UserConnectionMessage;
import co.weeby.event.UserDataAvailableMessage;
import co.weeby.event.UserRoomMessage;
import co.weeby.event.UserSendMessage;
import co.weeby.log.Log;
import co.weeby.terminal.Terminal;
import co.weeby.terminal.TerminalType;

public class MessageParserService extends Service {
	
	private static final String TAG ="MessageParserService";

	@Override
	public MessageEvent service(MessageEvent evt) {
		MessageEventType etype = evt.getEvtType();
		switch (etype) {
		case CLIENT_CONNECT_EVT:
			return handleClientConnection((UserConnectionMessage) evt);
		case USER_DATA_AVAI_EVT:
			return handleDataAvilable((UserDataAvailableMessage) evt);
		case SERVER_NODE_DATA_AVAI_EVT:
			return handleServerDataAvilable((ServerDataAvailableMessage) evt);
		case UNKOWN_TYPE:
			break;
		default:
			break;
		}
		return null;
	}

	private MessageEvent handleClientConnection(UserConnectionMessage us) {

		return us;
	}

	private MessageEvent handleDataAvilable(UserDataAvailableMessage us) {
		StringBuffer sb = new StringBuffer();
		byte[] buffer = new byte[1024];
		int n = -1;
		try {
			while ((n = us.getTerminal().read(buffer, 0, 1024)) > 0) {
				sb.append(new String(buffer, 0, n));
			}
			if (n == -1) {
				return new UserConnectionMessage(us.getTerminal(),
						UserConnectionMessage.DISCONNECT);
			}
			
			// Normal
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			us.getTerminal().setDataAvil(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return parseMessage(us.getTerminal(), sb.toString());
	}
	
	
	private MessageEvent handleServerDataAvilable(ServerDataAvailableMessage us) {
		StringBuffer sb = new StringBuffer();
		byte[] buffer = new byte[1024];
		int n = -1;
		try {
			while ((n = us.getTerminal().read(buffer, 0, 1024)) > 0) {
				sb.append(new String(buffer, 0, n));
			}
			
			us.getTerminal().setDataAvil(false);
			
			if (n == -1) {
				Log.e("MessageParserService", "Incorrect data len:" + n);
			}
			
			return parseMessage(us.getTerminal(), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			us.getTerminal().setDataAvil(false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	static byte[] CTRL= new byte[] {-1, -12, -1, -3, 6};

	private MessageEvent parseMessage(Terminal terminal, String str) {
		str = str.replaceAll("\r\n", "").trim();
		if (str.length() <= 0) {
			return null;
		}
		
		if (str.trim().equals(new String(CTRL))) {
			Log.e(TAG, " Terminal quit by CTRL+C ");
			return new UserConnectionMessage(terminal,
					UserConnectionMessage.DISCONNECT);
		}
		MessageEvent mv = null;
		char c = str.charAt(0);
		switch (c) {
		case '/':
			mv = parseUserMessage(terminal, str);
			break;
		case '@':
			mv = parseInternalMessage(terminal, str);
			break;
		default:
			UserSendMessage usm = new UserSendMessage(terminal);
			usm.setBroadcast(true);
			usm.setMsg(str);
			mv = usm;
			break;
		}
		return mv;

	}
	
	private MessageEvent parseUserMessage(Terminal terminal, String str) {
		if (str.startsWith("/join")) {
			if (str.trim().length() <= 6) {
				return new FallbackMessage(terminal, "No room names \n");
			} else if (str.charAt(5) != ' ') {
				return new FallbackMessage(terminal, "Incorrect format  shoule be /join roomname \n");
			} else {
				String roomName = str.substring(6).trim();
				return new UserRoomMessage(terminal, UserRoomMessage.Action.ENTER, roomName);
			}
			
		} else if (str.startsWith("/leave")) {
			return new UserRoomMessage(terminal, UserRoomMessage.Action.LEAVE, null);
		} else if (str.equalsIgnoreCase("/quit")) {
			return new UserConnectionMessage(terminal,
					UserConnectionMessage.DISCONNECT);
		} else if (str.equalsIgnoreCase("/rooms")) {
			return new UserRoomMessage(terminal, UserRoomMessage.Action.LIST, null);
		}
		return new FallbackMessage(terminal, "Unknown command : [/join  /leave /quit /rooms] \n");
	}
	
	
	private MessageEvent parseInternalMessage(Terminal terminal, String str) {
		if (terminal.getTerminalType() == TerminalType.CLIENT) {
			return new FallbackMessage(terminal, " Key workd reserved. Use by \\@ \n");
		}
		String[] arrs = str.split(" ");
		if (arrs.length <= 1) {
			return null;
		}
		String userName = arrs[1];
		
		if (str.startsWith("@/join")) {
			if (arrs.length < 3) {
				return null;
			}
			String roomName = arrs[2];
			return new InternalUserRoomMessage(terminal, InternalUserRoomMessage.Action.ENTER, userName, roomName);
		} else if (str.startsWith("@/leave")) {
			if (arrs.length < 3) {
				return null;
			}
			String roomName = arrs[2];
			return new InternalUserRoomMessage(terminal, InternalUserRoomMessage.Action.LEAVE, userName, roomName);
		} else if (str.startsWith("@/online")) {
			return new InternalUserStateMessage(terminal, userName, true);
		} else if (str.startsWith("@/offline")) {
			return new InternalUserStateMessage(terminal, userName, false);
		} else if (str.startsWith("@/send")) {
			if (arrs.length < 3) {
				return null;
			}
			String msg = str.substring(str.indexOf(arrs[1]) + arrs[1].length() + 1);
			return new InternalUserSendMessage(terminal, userName, msg);
		}
		return null;
	}
	
	
}
