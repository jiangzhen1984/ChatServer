package co.weeby.event;

public abstract class MessageEvent {
	
	protected long mvId;
	
	
	protected MessageEventType evtType;


	public MessageEvent(long mvId, MessageEventType evtType) {
		super();
		this.mvId = mvId;
		this.evtType = evtType;
	}


	public long getMvId() {
		return mvId;
	}


	public MessageEventType getEvtType() {
		return evtType;
	}


	@Override
	public String toString() {
		return "MessageEvent [mvId=" + mvId + ", evtType=" + evtType + "]";
	}
	
	
	
	
}
