package co.weeby.terminal;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Terminal {
	
	protected TerminalType terminalType;

	protected SocketAddress localAddres;
	
	protected SocketAddress remoteAddres;

	protected Socket socket;
	
	protected SocketChannel channel;

	protected boolean isDataAvil;
	
	protected SelectionKey selectionKey;
	
	protected ByteBuffer buffer;
	
	protected int len = -1;
	
	
	public Terminal(Socket socket) {
		this.socket = socket;
		if (socket != null) {
			this.localAddres = socket.getLocalSocketAddress();
			this.remoteAddres = socket.getRemoteSocketAddress();
		}
		this.buffer = ByteBuffer.allocate(1024);
	}
	
	public Terminal(SocketChannel sc) {
		this(sc.socket());
		this.channel = sc;
	}

	public  boolean isDataAvil() {
		return isDataAvil;
	}

	public void setDataAvil(boolean isDataAvil) throws IOException {
		this.isDataAvil = isDataAvil;
		if (isDataAvil) {
			len = this.channel.read(buffer);
			buffer.position(0);
		} else {
			buffer.clear();
			len = 0;
		}
	}

	public SocketAddress getLocalAddres() {
		return localAddres;
	}

	public SocketAddress getRemoteAddres() {
		return remoteAddres;
	}

	public TerminalType getTerminalType() {
		return terminalType;
	}
	
	
	
	
	
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	public int read(byte[] buf, int start, int n) throws IOException {
		if (this.channel != null) {
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
		} else {
			return this.socket.getInputStream().read(buf, start, n);
		}
	}
	
	public void write(byte[] data, int start, int len) throws IOException {
		if (this.channel != null) {
			this.channel.write(ByteBuffer.wrap(data, start, len));
		} else {
			this.socket.getOutputStream().write(data, start, len);
			this.socket.getOutputStream().flush();
		}
	}
	
	
	public void close() {
		if (socket != null) {
			if (this.selectionKey != null) {
				this.selectionKey.cancel();
			}
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
