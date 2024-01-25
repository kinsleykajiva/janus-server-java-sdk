package africa.jopen.sdk.models.events;


public class JanusTransportOriginatedEvent {
	
	/**
	 * The root record for a Janus transport-originated event, containing information about
	 * the emitter, event type, timestamp, and the nested event details.
	 */
	public record Root(String emitter, int type, long timestamp, JanusTransportOriginatedEvent.Event event) {
	}
	
	/**
	 * The record representing an event originated from Janus transport, including details
	 * about the transport, event ID, and associated data.
	 */
	public record Event(String transport, String id, JanusTransportOriginatedEvent.Data data) {
	}
	
	/**
	 * The record representing data associated with a Janus transport-originated event,
	 * including the event name, admin API status, IP address, and port.
	 */
	public record Data(String event, boolean admin_api, String ip, int port) {
	}
	
	
	
	
	public String trackInsert(JanusTransportOriginatedEvent.Root root) {
    String emitter = root.emitter();
    int type = root.type();
    long timestamp = root.timestamp();
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
