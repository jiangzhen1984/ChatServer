package co.weeby.service;

import co.weeby.event.FallbackMessage;
import co.weeby.event.InternalUserRoomMessage;
import co.weeby.event.InternalUserStateMessage;
import co.weeby.event.MessageEvent;
import co.weeby.event.MessageEventType;
import co.weeby.event.UserConnectionMessage;
import co.weeby.event.UserRoomMessage;
import co.weeby.event.UserSendMessage;

public class SyncService extends Service {
	
	private static final String TAG = "SyncService";

	@Override
	public MessageEvent service(MessageEvent evt) {
		MessageEventType ty = evt.getEvtType();
//		switch (ty) {
//		case USER_ROOM_EVT:
//			handleUserRoom((UserRoomMessage)evt);
//			break;
//		case CLIENT_SEND_MESSAGE_EVT:
//			handleUserSendMessage((UserSendMessage)evt);
//			break;
//		case CLIENT_CONNECT_EVT:
//			handleUserConnect((UserConnectionMessage) evt);
//			break;
//		case FALLBACK_EVT:
//			handleFallback((FallbackMessage) evt);
//			break;
//		case INTERNAL_USER_STATE_EVT:
//			handleInternalUserStateMessage((InternalUserStateMessage) evt);
//			break;
//		case INTERNAL_USER_ROOM_EVT:
//			handleInternalUserRoomMessage((InternalUserRoomMessage) evt);
//			
//		default:
//			break;
//		}
		return evt;

	}

}
