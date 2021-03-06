package co.weeby.connector;

/**
 * 
 * @author jiangzhen
 *
 */
public interface Connector {
	
	
	/**
	 * Use to start this connector
	 * @param config
	 */
	public boolean start(Configuration config);
	
	
	/**
	 * Use to destroy this connector
	 */
	public void destroy();

}
