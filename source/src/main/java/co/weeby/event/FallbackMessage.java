package co.weeby.event;

import co.weeby.terminal.Terminal;

public class FallbackMessage extends MessageEvent {
	
	private Terminal terminal;
	private String msg;

	
	public FallbackMessage(Terminal terminal, String msg) {
		super(System.currentTimeMillis(), MessageEventType.FALLBACK_EVT);
		this.terminal = terminal;
		this.msg = msg;
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
