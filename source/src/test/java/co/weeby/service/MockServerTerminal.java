package co.weeby.service;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import co.weeby.terminal.ServerTerminal;

public class MockServerTerminal extends ServerTerminal {

	public MockServerTerminal(DatagramChannel sc) {
		super(sc);
	}

	public MockServerTerminal(Socket socket) {
		super(socket);
	}

	public MockServerTerminal(SocketChannel sc) {
		super(sc);
	}

	@Override
	public int read(byte[] buf, int start, int n) throws IOException {
		if (len >= n) {
			System.arraycopy(src, pos, buf, start, n);
			len = len - n;
			return n;
		} else {
			System.arraycopy(src, pos, buf, start, len);
			int newLen = len;
			len = 0;
			return newLen;
			
		}
	}

	byte[] src = null;
	private String mockMsg;
	private int pos = 0;
	private int len = 0;


	public String getMockMsg() {
		return mockMsg;
	}

	public void setMockMsg(String mockMsg) {
		this.mockMsg = mockMsg;
		src = this.mockMsg.getBytes();
		pos = 0;
		len = src.length;
	}
	
	
	
	
}
