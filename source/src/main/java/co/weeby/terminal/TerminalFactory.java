package co.weeby.terminal;

import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public class TerminalFactory {
	
	
	public static ClientTerminal constructClientTerminal(SocketChannel socket) {
		if (socket == null) {
			throw new NullPointerException(" socket is null ");
		}
		return new ClientTerminal(socket);
	}
	
	public static ClientTerminal constructClientTerminal(Socket socket) {
		if (socket == null) {
			throw new NullPointerException(" socket is null ");
		}
		return new ClientTerminal(socket);
	}
	
	
	public static ServerTerminal constructServerNodeTerminal(Socket socket) {
		if (socket == null) {
			throw new NullPointerException(" socket is null ");
		}
		return new ServerTerminal(socket);
	}
	
	public static ServerTerminal constructServerNodeTerminal(DatagramChannel channel) {
		if (channel == null) {
			throw new NullPointerException(" channel is null ");
		}
		return new ServerTerminal(channel);
	}

}
