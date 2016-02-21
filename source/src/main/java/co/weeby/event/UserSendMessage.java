package co.weeby.event;

import co.weeby.terminal.Terminal;

public class UserSendMessage extends MessageEvent {
	
	private Terminal terminal;
	
	private Terminal target;
	
	private boolean broadcast;
	
	private String msg;

	public UserSendMessage(Terminal terminal) {
		super(System.currentTimeMillis(), MessageEventType.CLIENT_SEND_MESSAGE_EVT);
		this.terminal = terminal;
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public Terminal getTarget() {
		return target;
	}

	public void setTarget(Terminal target) {
		this.target = target;
	}

	public boolean isBroadcast() {
		return broadcast;
	}

	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	
	
}
