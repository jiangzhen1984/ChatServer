package co.weeby.event;

import co.weeby.terminal.ServerTerminal;

public class ServerDataAvailableMessage extends InternalSyncMessage {

	public ServerDataAvailableMessage(ServerTerminal node) {
		super(System.currentTimeMillis(), MessageEventType.SERVER_NODE_DATA_AVAI_EVT);
		this.terminal = node;
	}

	

}
