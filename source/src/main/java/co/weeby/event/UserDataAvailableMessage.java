package co.weeby.event;

import co.weeby.terminal.Terminal;

public class UserDataAvailableMessage extends MessageEvent {

	
	private Terminal terminal;

	public UserDataAvailableMessage(long mvId, Terminal terminal) {
		super(mvId, MessageEventType.CLIENT_CONNECT_EVT);
		this.terminal = terminal;
		
	}
	
	public UserDataAvailableMessage(Terminal terminal) {
		super(System.currentTimeMillis(), MessageEventType.USER_DATA_AVAI_EVT);
		this.terminal = terminal;
	}

	public Terminal getTerminal() {
		return terminal;
	}
	
}
