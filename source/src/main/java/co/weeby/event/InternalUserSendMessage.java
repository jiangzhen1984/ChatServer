package co.weeby.event;

import co.weeby.terminal.Terminal;

public class InternalUserSendMessage extends InternalSyncMessage {
	
	
	
	private String userName;
	private String msg;
	
	public InternalUserSendMessage(Terminal terminal, String nickName, String msg) {
		super(System.currentTimeMillis(), MessageEventType.INTERNAL_USER_SEND_MSG_EVT);
		this.terminal = terminal;
		this.userName = nickName;
		this.msg = msg;
	}



	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}



	public String getMsg() {
		return msg;
	}



	public void setMsg(String msg) {
		this.msg = msg;
	}


	
}
