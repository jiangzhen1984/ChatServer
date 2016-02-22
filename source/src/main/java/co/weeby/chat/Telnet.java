package co.weeby.chat;

import co.weeby.terminal.ClientTerminal;

public class Telnet  {
	
	private ClientTerminal terminal;
	private String nickName;
	
	private Room room;
	
	private boolean isLocal;
	
	private boolean firstInit;
	

	public Telnet(ClientTerminal terminal) {
		this.terminal = terminal;
	}

	
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}


	public ClientTerminal getTerminal() {
		return terminal;
	}


	public void setTerminal(ClientTerminal terminal) {
		this.terminal = terminal;
	}


	public Room getRoom() {
		return room;
	}


	public void setRoom(Room room) {
		this.room = room;
	}

	
	public boolean isAvl() {
		return nickName != null && room != null;
	}


	public boolean isLocal() {
		return isLocal;
	}


	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}


	public boolean isFirstInit() {
		return firstInit;
	}


	public void setFirstInit(boolean firstInit) {
		this.firstInit = firstInit;
	}
	
	
}
