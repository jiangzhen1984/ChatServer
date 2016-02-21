package co.weeby.service;

import co.weeby.event.MessageEvent;

public interface ServiceChain {

	
	public void doChain(MessageEvent event);
}
