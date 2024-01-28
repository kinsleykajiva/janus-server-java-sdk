package africa.jopen.sdk.models.events;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;

/**
 * Represents a Janus event.
 */
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
	
	
	public String trackInsert() {
    StringBuilder sql = new StringBuilder();
		var timestamp = new Timestamp(this.getTimestamp() / 1000);
    if (event instanceof VideoRoomPluginEventData) {
        sql.append("INSERT INTO janus_plugins (session, handle, plugin, event, timestamp) VALUES (")
            .append(this.getSession_id()).append(", ")
            .append(this.getHandle_id()).append(", '")
            .append(this.getEvent().getPlugin()).append("', '")
            .append(((VideoRoomPluginEventData) event).getEvent()).append("', '")
            .append(timestamp).append("');");

        JSONArray streamsJ = new JSONArray();
        if (((VideoRoomPluginEventData) event).getStream() != null) {
            for (int i = 0; i < ((VideoRoomPluginEventData) event).getStream().length; i++) {
                JSONObject stream = new JSONObject();
                stream.put("type", ((VideoRoomPluginEventData) event).getStream()[i].getType());
                stream.put("mindex", ((VideoRoomPluginEventData) event).getStream()[i].getMindex());
                stream.put("mid", ((VideoRoomPluginEventData) event).getStream()[i].getMid());
                stream.put("codec", ((VideoRoomPluginEventData) event).getStream()[i].getCodec());
                stream.put("ready", ((VideoRoomPluginEventData) event).getStream()[i].isReady());
                stream.put("send", ((VideoRoomPluginEventData) event).getStream()[i].isSend());
                stream.put("feed_id", ((VideoRoomPluginEventData) event).getStream()[i].getFeed_id());
                stream.put("feed_display", ((VideoRoomPluginEventData) event).getStream()[i].getFeed_display());
                stream.put("feed_mid", ((VideoRoomPluginEventData) event).getStream()[i].getFeed_mid());
                streamsJ.put(stream);
            }
        }

        sql.append("INSERT INTO janus_videoroom_plugin_event(session,handle,data_id,data_private_id,display,room,opaque_id,streams_array,timestamp) VALUES(")
            .append(this.getSession_id()).append(", ")
            .append(this.getHandle_id()).append(", '").append(((VideoRoomPluginEventData) event).getId()).append("', '")
            .append(((VideoRoomPluginEventData) event).getPrivate_id()).append("', '")
            .append(((VideoRoomPluginEventData) event).getPrivate_id()).append("', '")
            .append(((VideoRoomPluginEventData) event).getRoom()).append("', '")
            .append(((VideoRoomPluginEventData) event).getOpaque_id()).append("', '")
            .append(streamsJ.toString(2)).append("', '")
            .append(timestamp).append("' )");
		// clean up objects not in use for GC to collect
	    		streamsJ = null;
				
    }
    return sql.toString();
}
}
