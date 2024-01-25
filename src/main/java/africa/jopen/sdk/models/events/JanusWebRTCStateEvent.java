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
	public record Event(String ice, int stream_id, int component_id) {
	}
	
	/*CREATE TABLE IF NOT EXISTS janus_ice (
				    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
				    session BIGINT(30) NOT NULL,
				    handle BIGINT(30) NOT NULL,
				    stream INT NOT NULL,
				    component INT NOT NULL,
				    state VARCHAR(30) NOT NULL,
				    timestamp DATETIME NOT NULL,
				    INDEX janus_idx_session_handle (session, handle),
				    INDEX janus_idx_stream_component (stream, component),
				    INDEX janus_idx_state (state),
				    INDEX janus_idx_timestamp (timestamp)
				);*/
	
	public String trackInsert( JanusWebRTCStateEvent.Root root ) {
		
		return "INSERT INTO janus_ice (session, handle, stream, component, state, timestamp) VALUES ("
				+ root.session_id() + ", "
				+ root.handle_id() + ", "
				+ root.event().stream_id() + ", "
				+ root.event().component_id() + ", '"
				+ root.event().ice() + "', '"
				+ new Timestamp(root.timestamp()) + "')";
	}
}
