package africa.jopen.sdk.models.events;

public class JanusEvent {
	private String emitter;
	private int type;
	private int subtype;
	private long timestamp;
	private long session_id;
	private long handle_id;
	private String opaque_id;
	
	private Event event;
	
	public Event getEvent() {
		return event;
	}
	
	public void setEvent( Event event ) {
		this.event = event;
	}
	
	public String getEmitter() {
		return emitter;
	}
	
	public void setEmitter( String emitter ) {
		this.emitter = emitter;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType( int type ) {
		this.type = type;
	}
	
	public int getSubtype() {
		return subtype;
	}
	
	public void setSubtype( int subtype ) {
		this.subtype = subtype;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}
	
	public long getSession_id() {
		return session_id;
	}
	
	public void setSession_id( long session_id ) {
		this.session_id = session_id;
	}
	
	public long getHandle_id() {
		return handle_id;
	}
	
	public void setHandle_id( long handle_id ) {
		this.handle_id = handle_id;
	}
	
	public String getOpaque_id() {
		return opaque_id;
	}
	
	public void setOpaque_id( String opaque_id ) {
		this.opaque_id = opaque_id;
	}
}
