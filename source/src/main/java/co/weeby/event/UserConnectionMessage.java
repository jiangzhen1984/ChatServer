package co.weeby.event;

import co.weeby.terminal.Terminal;

public class UserConnectionMessage extends MessageEvent {
	
	public static final int CONNECT = 1;
	public static final int DISCONNECT = 2;
	
	private Terminal terminal;
	
	private int state;

	public UserConnectionMessage(long mvId, Terminal terminal, int state) {
		super(mvId, MessageEventType.CLIENT_CONNECT_EVT);
		this.terminal = terminal;
		this.state = state;
		
	}
	
	public UserConnectionMessage(Terminal terminal, int state) {
		this(System.currentTimeMillis(), terminal, state);
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public int getState() {
		return state;
	}
	
	

}
