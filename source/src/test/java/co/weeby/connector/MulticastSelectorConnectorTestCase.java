package co.weeby.connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import junit.framework.TestCase;
import co.weeby.event.MessageEvent;
import co.weeby.service.ServiceLooper;

public class MulticastSelectorConnectorTestCase extends TestCase {

	Connector conn;

	protected void setUp() throws Exception {
		super.setUp();
		conn = new MulticastSelectorConnector(new ServiceLooper() {
			public void handleMessage(MessageEvent evt) {
			}
		});
	}

	public void testStart() {

		Configuration config = new Configuration();
		config.multicastAddress = "228.254.254.254";
		config.multicastPort = 9499;
		config.multicastIface = "wlan1";
		config.useMulicast = true;
		conn.start(config);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			byte[] data = "/online aaaa".getBytes();

			InetAddress group = InetAddress.getByName(config.multicastAddress);
			MulticastSocket s = new MulticastSocket();
			DatagramPacket hi = new DatagramPacket(data, data.length, group,
					config.multicastPort);
			s.send(hi);
			s.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		conn.destroy();
	}

	public void testDestroy() {
	}

}
