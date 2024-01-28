package africa.jopen.sdk.models.events;


import java.sql.Timestamp;

/**
 * Represents a Janus transport-originated event.
 * This class contains records for the root event, the event itself, and the associated data.
 */
public class JanusTransportOriginatedEvent {
	
	/**
	 * The root record for a Janus transport-originated event, containing information about
	 * the emitter, event type, timestamp, and the nested event details.
	 * @param  emitter The emitter of the Janus transport-originated event.
	 * @param  type The type of the Janus transport-originated event.
	 * @param  timestamp The timestamp of the Janus transport-originated event.
	 * @param  event The nested event details of the Janus transport-originated event.
	 */
	public record Root(String emitter, int type, long timestamp, JanusTransportOriginatedEvent.Event event) {
	}
	
	/**
	 * The record representing an event originated from Janus transport, including details
	 * about the transport, event ID, and associated data.
	 * @param transport The transport details of the Janus transport-originated event.
	 * @param id The event ID of the Janus transport-originated event.
	 * @param data The associated data of the Janus transport-originated event.
	 */
	public record Event(String transport, String id, JanusTransportOriginatedEvent.Data data) {
	}
	
	/**
	 * The record representing data associated with a Janus transport-originated event,
	 * including the event name, admin API status, IP address, and port.
	 *
	 * @param event The name of the event.
	 * @param admin_api The admin API status.
	 * @param ip The IP address.
	 * @param port The port.
	 */
	public record Data(String event, boolean admin_api, String ip, int port) {
	}
	
	/**
	 * Inserts the Janus transport-originated event into the database.
	 * 
	 * @param root The root event containing the emitter, event type, timestamp, and event details.
	 * @return The SQL query for inserting the event into the database.
	 * @see #trackInsert(JanusTransportOriginatedEvent.Root)
	 *
	 */
	public String trackInsert(JanusTransportOriginatedEvent.Root root) {
    String emitter = root.emitter();
    int type = root.type();
		var timestamp = new Timestamp(root.timestamp() / 1000);
    JanusTransportOriginatedEvent.Event event = root.event();

    String transport = event.transport();
    String id = event.id();
    JanusTransportOriginatedEvent.Data data = event.data();

    String eventName = data.event();
    boolean adminApi = data.admin_api();
    String ip = data.ip();
    int port = data.port();
		
		return "INSERT INTO janus_transports (emitter, type, timestamp, transport, id, event, admin_api, ip, port) " +
                 "VALUES ('" + emitter + "', " + type + ", " + timestamp + ", '" + transport + "', '" + id + "', '" + eventName + "', " + adminApi + ", '" + ip + "', " + port + ")";
}
}
