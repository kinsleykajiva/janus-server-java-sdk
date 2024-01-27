package africa.jopen.sdk.models.events;


/**
 * Represents a Janus handle event, containing information about the emitter,
 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
 */
public class JanusHandleEvent {
	
	/**
	 * The root record for a Janus handle event, containing information about the emitter,
	 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
	 */
	public record Root(String emitter, int type, long timestamp, long session_id, long handle_id, String opaque_id, JanusHandleEvent.Event event) {
	}
	
	/**
	 * The record representing a handle event within Janus, including the name, associated plugin,
	 * and opaque ID details.
	 */
	public record Event(String name, String plugin, String opaque_id) {
	}
	
	/**
	 * Generates an SQL INSERT statement for tracking a Janus handle event.
	 * 
	 * @param root The root record containing the event details.
	 * @return The SQL INSERT statement.
	 */
	public String trackInsert(JanusHandleEvent.Root root){
    return String.format(
        "INSERT INTO janus_handles (session, handle, event, plugin, timestamp) VALUES (%d, %d, '%s', '%s', FROM_UNIXTIME(%d))",
        root.session_id(), root.handle_id(), root.event().name(), root.event().plugin(), root.timestamp()
    );
}

}
