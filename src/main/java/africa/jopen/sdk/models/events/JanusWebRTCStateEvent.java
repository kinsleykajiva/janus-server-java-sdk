package africa.jopen.sdk.models.events;


import java.sql.Timestamp;

public class JanusWebRTCStateEvent {
	
	/**
	 * The root record for a Janus WebRTC state event, containing information about the emitter,
	 * event type, subtype, timestamp, session ID, handle ID, opaque ID, and the nested event details.
	 */
	public record Root(String emitter, int type, int subtype, long timestamp, long session_id, long handle_id, String opaque_id, JanusWebRTCStateEvent.Event event) {
	}
	
	/**
	 * The record representing a WebRTC state event within Janus, including ICE (Interactive
	 * Connectivity Establishment) details, stream ID, and component ID.
	 */
	public record Event(String ice, int stream_id, int component_id,String local_candidate,String remote_candidate) {
	}
	

	
	public String trackInsert( JanusWebRTCStateEvent.Root root ) {
		
		return "INSERT INTO janus_ice (session, handle, stream, component, state, timestamp,local_candidate,remote_candidate) VALUES ("
				+ root.session_id() + ", "
				+ root.handle_id() + ", "
				+ root.event().stream_id() + ", "
				+ root.event().component_id() + ", '"
				+ root.event().ice() + "', '"
				+ new Timestamp(root.timestamp()) + "' , '" +
				root.event().local_candidate() + "','" +
				root.event().remote_candidate() + "'); ";
				
	}
}
