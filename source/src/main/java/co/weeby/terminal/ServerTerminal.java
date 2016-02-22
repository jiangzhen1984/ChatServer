package co.weeby.terminal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public class ServerTerminal extends Terminal {
	
	protected DatagramChannel sc;

	public ServerTerminal(Socket socket) {
		super(socket);
		this.terminalType = TerminalType.SERVER;
	}

	public ServerTerminal(SocketChannel sc) {
		super(sc);
		this.terminalType = TerminalType.SERVER;
	}
	
	public ServerTerminal(DatagramChannel sc) {
		super((Socket)null);
		this.terminalType = TerminalType.SERVER;
		this.sc = sc;
	}

	@Override
	public void setDataAvil(boolean isDataAvil) throws IOException {
		this.isDataAvil = isDataAvil;
		if (isDataAvil) {
			InetSocketAddress sender= (InetSocketAddress)this.sc.receive(buffer);
			len = buffer.position();
			buffer.position(0);
		} else {
			buffer.clear();
			len = 0;
		}
	}
	
	
	public int read(byte[] buf, int start, int n) throws IOException {
		if (len == -1 || len == 0) {
			return len;
		}
		if (len > (n - start)) {
			buffer.get(buf, start, n);
			len = len - (n - start);
			return n - start;
		} else {
			buffer.get(buf, start, len);
			n = len;
			len = 0;
			return n;
		}
		
	}
	
	public void write(byte[] data, int start, int len) throws IOException {
		
	}

	
	
	
}
