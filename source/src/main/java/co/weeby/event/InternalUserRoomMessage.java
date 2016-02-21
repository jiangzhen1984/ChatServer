package co.weeby.event;

import co.weeby.terminal.Terminal;

public class InternalUserRoomMessage extends InternalSyncMessage {

	private Terminal terminal;
	private Action ac;
	private String room;
	private String userName;
	
	public InternalUserRoomMessage(Terminal terminal, Action flag, String userName, String room) {
		super(System.currentTimeMillis(), MessageEventType.INTERNAL_USER_ROOM_EVT);
		this.room = room;
		this.userName = userName;
		this.ac = flag;
	}
	
	
	
	
	
	public String getUserName() {
		return userName;
	}





	public void setUserName(String userName) {
		this.userName = userName;
	}





	public Action getAc() {
		return ac;
	}





	public void setAc(Action ac) {
		this.ac = ac;
	}





	public String getRoom() {
		return room;
	}





	public void setRoom(String room) {
		this.room = room;
	}





	public Terminal getTerminal() {
		return terminal;
	}





	public enum Action {
		ENTER,
		LEAVE,
		LIST;
	}

}
