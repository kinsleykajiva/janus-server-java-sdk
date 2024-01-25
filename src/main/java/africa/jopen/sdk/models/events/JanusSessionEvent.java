package africa.jopen.sdk.models.events;


public class JanusSessionEvent {
	
	/**
	 * The root record for a Janus session event, containing information about the emitter,
	 * event type, timestamp, session ID, and the nested event details.
	 */
	public record Root(String emitter, int type, long timestamp, long session_id, JanusSessionEvent.Event event) {
	}
	
	/**
	 * The record representing an event within a Janus session, including the event name
	 * and the associated transport details.
	 */
	public record Event(String name, JanusSessionEvent.Transport transport) {
	}
	
	/**
	 * The record representing the transport details of a Janus session event, including
	 * the transport type and ID.
	 */
	public record Transport(String transport, long id) {
	}
	
	
	public String trackInsert( JanusSessionEvent.Root root ) {
		return String.format(
				"INSERT INTO janus_sessions (session, event, timestamp) VALUES (%d, '%s', FROM_UNIXTIME(%d))",
				root.session_id(), root.event().name(), root.timestamp()
		);
	}
}


