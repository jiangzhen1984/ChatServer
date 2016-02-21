package co.weeby.chat;

import java.util.ArrayList;
import java.util.List;

public class Room {
	
	private String name;
	
	private List<Telnet> users;
	
	
	
	public Room(String name) {
		super();
		this.name = name;
		 users = new ArrayList<Telnet>();
	}


	public Room() {
		 users = new ArrayList<Telnet>();
	}
	
	
	public void addUser(Telnet terminal) {
		users.add(terminal);
	}
	
	
	public void removeUser(Telnet ter) {
		users.remove(ter);
	}
	
	
	public int getUserCount() {
		return users.size();
	}


	public String getName() {
		return name;
	}


	public List<Telnet> getUsers() {
		return users;
	}
	
	
	
}
