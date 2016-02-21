package co.weeby.service;

import co.weeby.event.MessageEvent;

/**
 * Message looper for handle Message.<br>
 * All messages should come from {@link co.weeby.connector.Connector} .
 * 
 *  @see co.weeby.connector.Connector
 *  @see co.weeby.connector.SelectorConnector
 * 
 *  @author jiangzhen
 */
public interface ServiceLooper {
	
	
	/**
	 * Handle Message which from connector API
	 * @param evt
	 */
	public void handleMessage(MessageEvent evt);

}
