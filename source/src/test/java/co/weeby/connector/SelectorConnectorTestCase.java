package co.weeby.connector;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

import junit.framework.TestCase;
import co.weeby.event.MessageEvent;
import co.weeby.service.ServiceLooper;

public class SelectorConnectorTestCase extends TestCase {

	Connector conn;

	protected void setUp() throws Exception {
		super.setUp();
		conn = new SelectorConnector(new ServiceLooper() {
			public void handleMessage(MessageEvent evt) {

			}
		});
	}

	public void testStart() {
		Configuration config = new Configuration();
		config.address = "127.0.0.1";
		config.port = 9939;
		conn.start(config);

		Socket sock = new Socket();
		try {
			sock.connect(new InetSocketAddress(Inet4Address
					.getByName(config.address), config.port));
		} catch (IOException e) {
			e.printStackTrace();
			fail("connect failed");
		}
		// try {
		// sock.getOutputStream().write("aaaa".getBytes());
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//

		//
		Socket sock1 = new Socket();
		try {
			sock1.connect(new InetSocketAddress(Inet4Address
					.getByName(config.address), config.port));
		} catch (IOException e) {
			e.printStackTrace();
			fail("connect failed");
		}
		//
		//
		//
		Socket sock2 = new Socket();
		try {
			sock2.connect(new InetSocketAddress(Inet4Address
					.getByName(config.address), config.port));
		} catch (IOException e) {
			e.printStackTrace();
			fail("connect failed");
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			sock.close();
			sock1.close();
			sock2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		conn.destroy();
	}

	public void testDestroy() {
	}

}
