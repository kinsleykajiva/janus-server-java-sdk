package africa.jopen.sdk.models.events;


public class JanusCoreEvent {
	
	/**
	 * The root record for a Janus core event, containing information about the emitter,
	 * event type, subtype, timestamp, and the nested event details.
	 *
	 * @param emitter The emitter of the event.
	 * @param type The type of the event.
	 * @param subtype The subtype of the event.
	 * @param timestamp The timestamp of the event.
	 * @param event The nested event details.
	 */
	public record Root(String emitter, int type, int subtype, long timestamp, JanusCoreEvent.Event event) {
	}
	
	/**
	 * The record representing a core event within Janus, including the status information
	 * and additional details provided by the nested Info record.
	 *
	 * @param status The status of the event.
	 * @param info The additional information associated with the event.
	 */
	public record Event(String status, JanusCoreEvent.Info info) {
	}
	
	/**
	 * The record representing information associated with a Janus core event, including
	 * the number of sessions, handles, peer connections, and the statistics period.
	 *
	 * @param sessions The number of sessions.
	 * @param handles The number of handles.
	 * @param peerconnections The number of peer connections.
	 * @param stats_period The statistics period.
	 */
	public record Info(long sessions, long handles, long peerconnections, long stats_period) {
	}
	
	/**
	 * SQL statement to insert a Janus core event into the database.
	 * @param root The root record for a Janus core event.
	 *
	 * */
	public String trackInsert( JanusCoreEvent.Root root ) {
		return String.format(
				"INSERT INTO janus_core (name, value, timestamp) VALUES ('%s', '%s', FROM_UNIXTIME(%d))",
				root.emitter(), root.event().status(), root.timestamp()
		);
		
	}
}
