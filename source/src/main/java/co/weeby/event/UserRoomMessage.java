package co.weeby.event;

import co.weeby.terminal.Terminal;

public class UserRoomMessage extends MessageEvent {

	private Terminal terminal;
	private Action ac;
	private String room;
	
	public UserRoomMessage(Terminal terminal, Action flag, String room) {
		super(System.currentTimeMillis(), MessageEventType.USER_ROOM_EVT);
		this.terminal = terminal;
		this.ac = flag;
		this.room = room;
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
