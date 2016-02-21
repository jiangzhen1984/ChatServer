package co.weeby.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import co.weeby.terminal.Terminal;

public class GlobalCache {
	
	private Map<String, Room> rooms;
	
	private Map<Terminal, Telnet> clientTeminals;
	
	private Map<String, Telnet> clients;
	
	private static GlobalCache instance;
	
	private GlobalCache() {
		rooms = new ConcurrentHashMap<String, Room>();
		clientTeminals = new ConcurrentHashMap<Terminal, Telnet>();
		clients = new ConcurrentHashMap<String, Telnet>();
	}
	
	
	public static GlobalCache getInstance() {
		if (instance == null) {
			instance = new GlobalCache();
		}
		
		return instance;
	}
	

	
	public List<Room> getRooms() {
		return new ArrayList<Room>(rooms.values());
	}
	
	
	public void addRoom(String name, Room room) {
		rooms.put(name, room);
	}
	
	public Room removeRoom(String name) {
		return rooms.remove(name);
	}
	
	
	public Room getRoom(String name) {
		return rooms.get(name);
	}
	
	

	public void saveTerminal(Terminal terminal, Telnet tel) {
		if (tel.isLocal()) {
			clientTeminals.put(terminal,  tel);
		}
		clients.put(tel.getNickName(),  tel);
	}
	
	
	public Telnet getTel(Terminal terminal) {
		return clientTeminals.get(terminal);
	}
	
	public Telnet getTel(String nickName) {
		return clients.get(nickName);
	}
	
	
	
	public void saveTel(String nickName, Telnet tel) {
		clients.put(tel.getNickName(),  tel);
		if (tel.isLocal()) {
			clientTeminals.put(tel.getTerminal(),  tel);
		}
	}
	
	
	public void removeTel(String nickName) {
		Telnet tel = clients.remove(nickName);
		if (tel != null) {
			clientTeminals.remove(tel.getTerminal());
		}
	}
	
	
	public void removeTel(Terminal clientTerminal) {
		Telnet tel =clientTeminals.remove(clientTerminal);
		if (tel != null) {
			 clients.remove(tel.getNickName());
		}
	}
}
