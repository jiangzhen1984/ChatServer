package co.weeby.event;

import co.weeby.terminal.Terminal;

public abstract class InternalSyncMessage extends MessageEvent {
	
	protected Terminal terminal;
	
	

	public InternalSyncMessage(long mvId, MessageEventType evtType) {
		super(mvId, evtType);
	}



	public Terminal getTerminal() {
		return terminal;
	}

	
}
