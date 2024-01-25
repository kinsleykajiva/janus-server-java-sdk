package africa.jopen.sdk.models.events;


public class JanusCoreEvent {
	
	/**
	 * The root record for a Janus core event, containing information about the emitter,
	 * event type, subtype, timestamp, and the nested event details.
	 */
	public record Root(String emitter, int type, int subtype, long timestamp, JanusCoreEvent.Event event) {
	}
	
	/**
	 * The record representing a core event within Janus, including the status information
	 * and additional details provided by the nested Info record.
	 */
	public record Event(String status, JanusCoreEvent.Info info) {
	}
	
	/**
	 * The record representing information associated with a Janus core event, including
	 * the number of sessions, handles, peer connections, and the statistics period.
	 */
	public record Info(long sessions, long handles, long peerconnections, long stats_period) {
	}
	
	public String trackInsert( JanusCoreEvent.Root root ) {
		return String.format(
				"INSERT INTO janus_core (name, value, timestamp) VALUES ('%s', '%s', FROM_UNIXTIME(%d))",
				root.emitter(), root.event().status(), root.timestamp()
		);
		
	}
}
