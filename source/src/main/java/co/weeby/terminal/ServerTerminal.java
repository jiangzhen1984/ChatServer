package co.weeby.terminal;

import java.net.Socket;
import java.nio.channels.SocketChannel;

public class ServerTerminal extends Terminal {

	public ServerTerminal(Socket socket) {
		super(socket);
		this.terminalType = TerminalType.SERVER;
	}

	public ServerTerminal(SocketChannel sc) {
		super(sc);
		this.terminalType = TerminalType.SERVER;
	}

}
