package co.weeby;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ForkJoinPool;

import co.weeby.connector.Configuration;
import co.weeby.connector.Connector;
import co.weeby.connector.MulticastSelectorConnector;
import co.weeby.connector.SelectorConnector;
import co.weeby.log.Log;
import co.weeby.service.DefaultServiceLooper;
import co.weeby.service.MessageParserService;
import co.weeby.service.OrderServiceChain;
import co.weeby.service.OutputService;
import co.weeby.service.ServiceLooper;
import co.weeby.service.SyncService;

public class Main {

	
	
	public boolean parse(String[]  args, Configuration config) {
		config.address = "localhost";
		config.port = 9399;
		boolean port = false;
		boolean address = false;
		boolean mp = false;
		boolean mh = false;
		boolean mi = false;
		for (int i = 0; i < args.length; i++) {
			if (port) {
				port = false;
				try {
					config.port = Integer.parseInt(args[i]);
					if (config.port <= 1 || config.port > 65534) {
						Log.e("main", "===> port must in range(2-65534) \n");
						return false;
					}
					
				} catch (NumberFormatException e) {
					Log.e("main", "===> port must be number \n");
					return false;
				}
				
			}
			if (address) {
				address = false;
				config.address = args[i];
			}
			
			if (mh) {
				mh = false;
				config.multicastAddress = args[i];
				try {
					boolean ret = InetAddress.getByName(config.multicastAddress).isMulticastAddress();
					if (!ret) {
						Log.e("main", "===>multicast address is incorrect \n");
						return false;
					}
				} catch (UnknownHostException e) {
					Log.e("main", "===>multicast address unknown \n");
					return false;
				}
			}
			
			if (mi) {
				mi = false;
				config.multicastIface = args[i];
				try {
					NetworkInterface.getByName(config.multicastIface);
				} catch (SocketException e) {
					Log.e("main", "===> load multicast iface failed \n");
					return false;
				}
			}
			
			if (mp) {
				mp = false;
				try {
					config.multicastPort = Integer.parseInt(args[i]);
					if (config.multicastPort <= 1 || config.port > 65534) {
						Log.e("main", "===> port must in range(2-65534) \n");
						return false;
					}
					
				} catch (NumberFormatException e) {
					Log.e("main", "===> multicastPort must be number \n");
					return false;
				}
			}
			
			if (args[i].equals("-p")) {
				port = true;
			}
			
			if (args[i].equals("-h")) {
				address = true;
			}
			
			if (args[i].equals("-mp")) {
				mp = true;
			}
			
			if (args[i].equals("-mh")) {
				mh = true;
			}
			
			if (args[i].equals("-mi")) {
				mi = true;
			}
			
		}
		
		if (config.multicastAddress != null && config.multicastIface != null && config.multicastPort > 0) {
			config.useMulicast = true;
		}
		
		return true;
	}
	
	
	public void start(Configuration config) {

		ForkJoinPool pool = new ForkJoinPool();
		OrderServiceChain  chain = new OrderServiceChain();
		chain.addService(new MessageParserService());
		chain.addService(new OutputService());
		
		
		ServiceLooper looper = new DefaultServiceLooper(pool, chain);
		Connector connector = new SelectorConnector(looper);
		connector.start(config);
		
		if (config.useMulicast) {
			MulticastSelectorConnector multi = new MulticastSelectorConnector(looper);
			chain.addService(new SyncService(multi.getMulicastChannel(), config));
			multi.start(config);
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Configuration config = new Configuration();
		Main main = new Main();
		if (!main.parse(args, config)) {
			Log.e("main", "===> java -jar jarname -p port -h listen -mp multicast-port -mh group -mi groupiface");
			return ;
		}
		
		main.start(config);
		
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
