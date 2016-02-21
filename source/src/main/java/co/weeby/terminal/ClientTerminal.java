package co.weeby.terminal;

import java.net.Socket;
import java.nio.channels.SocketChannel;

public class ClientTerminal extends Terminal {
	
	
	public ClientTerminal(Socket socket) {
		super(socket);
		this.terminalType = TerminalType.CLIENT;
	}

	public ClientTerminal(SocketChannel sc) {
		super(sc);
		this.terminalType = TerminalType.CLIENT;
	}
}
