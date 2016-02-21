package co.weeby.service;

import java.util.concurrent.ExecutorService;

import co.weeby.event.MessageEvent;
import co.weeby.log.Log;

public class DefaultServiceLooper implements ServiceLooper {
	
	private ExecutorService executor;
	private ServiceChain chain;
	
	

	public DefaultServiceLooper(ExecutorService executor, ServiceChain chain) {
		super();
		this.executor = executor;
		this.chain = chain;
	}



	public void handleMessage(MessageEvent evt) {
		if (this.executor == null) {
			throw new NullPointerException(" executor is null");
		}
		if (executor.isShutdown()) {
			throw new IllegalStateException(" executor is shutdowned ");
		}

		if (chain == null) {
			Log.e("DefaultServiceLooper", " chain is null can not call do chain");
			return;
		}
		executor.submit(new LocalTask(chain, evt));
	}
	
	
	
	class LocalTask implements Runnable {
		MessageEvent evt;
		ServiceChain chain;
		
		public LocalTask(ServiceChain chain ,MessageEvent evt) {
			super();
			this.evt = evt;
			this.chain = chain;
		}


		public void run() {
			try {
			chain.doChain(evt);
			} catch(Exception e) {
				Log.e("DefaultServiceLooper", "service failed", e);
			}
			
		}
		
	}

}
