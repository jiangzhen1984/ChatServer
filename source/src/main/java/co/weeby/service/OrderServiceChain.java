package co.weeby.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import co.weeby.event.MessageEvent;

/**
 * Server Chain in order.<br>
 * Service 1 -> Service 2 -> Service 3... -> Service N
 * 
 * @see ServiceChain
 * 
 * @author jiangzhen
 *
 */
public class OrderServiceChain implements ServiceChain {

	
	private List<Service> chains;
	
	public OrderServiceChain() {
		super();
		chains = new ArrayList<Service>();
	}

	public void doChain(MessageEvent event) {
		MessageEvent returnMsgEvt = event;
		Iterator<Service> it = chains.iterator();
		while(it.hasNext()) {
			returnMsgEvt = it.next().service(returnMsgEvt);
			if (returnMsgEvt == null) {
				break;
			}
		}

	}

	
	
	public void addService(Service ser) {
		chains.add(ser);
	}
	
	public void addService(Service ser, int idx) {
		chains.add(idx, ser);
	}
	
	public boolean removeService(Service ser) {
		return chains.remove(ser);
	}
	
	public Service removeService(int idx) {
		return chains.remove(idx);
	}
}
