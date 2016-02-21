package co.weeby.event;

import co.weeby.terminal.Terminal;

public class InternalUserStateMessage extends InternalSyncMessage {
	
	
	private boolean online;
	private String userName;
	
	public InternalUserStateMessage(Terminal terminal, String nickName, boolean online) {
		super(System.currentTimeMillis(), MessageEventType.INTERNAL_USER_STATE_EVT);
		this.terminal = terminal;
		this.online = online;
		this.userName = nickName;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	
}
