package co.weeby;

import java.util.concurrent.ForkJoinPool;

import co.weeby.connector.Configuration;
import co.weeby.connector.Connector;
import co.weeby.connector.SelectorConnector;
import co.weeby.log.Log;
import co.weeby.service.DefaultServiceLooper;
import co.weeby.service.MessageParserService;
import co.weeby.service.OrderServiceChain;
import co.weeby.service.OutputService;

public class main {


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Configuration config = new Configuration();
		config.address = "localhost";
		config.port = 9399;
		boolean port = false;
		boolean address = false;
		for (int i = 0; i < args.length; i++) {
			if (port) {
				port = false;
				try {
					config.port = Integer.parseInt(args[i]);
					if (config.port <= 1 || config.port > 65534) {
						Log.e("main", "===> port must in range(2-65534) \n");
						return;
					}
					
				} catch (NumberFormatException e) {
					Log.e("main", "===> port must be number \n");
					return;
				}
				
			}
			if (address) {
				address = false;
				config.address = args[i];
			}
			if (args[i].equals("-p")) {
				port = true;
			}
			
			if (args[i].equals("-h")) {
				address = true;
			}
			
		}
		
		config.useMulicast = true;
		config.multicastAddress ="224.133.23.34";
		config.multicastPort = 9499;
		
		ForkJoinPool pool = new ForkJoinPool();
		OrderServiceChain  chain = new OrderServiceChain();
		chain.addService(new MessageParserService());
		chain.addService(new OutputService());
		Connector connector = new SelectorConnector(new DefaultServiceLooper(pool, chain));
		
		
		
		connector.start(config);
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
