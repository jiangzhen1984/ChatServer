package co.weeby.service;

import co.weeby.event.MessageEvent;
import co.weeby.log.Log;

public class LogService extends Service {

	@Override
	public MessageEvent service(MessageEvent evt) {
		Log.i("LogService", " ==> " + evt);
		return evt;
	}

}
