package co.weeby.service;

import co.weeby.event.MessageEvent;

public abstract class Service {

	/**
	 * Handle message event.
	 * @param evt new Message event for next link.
	 * @return null for break chain
	 */
	public abstract MessageEvent service(MessageEvent evt);
}
