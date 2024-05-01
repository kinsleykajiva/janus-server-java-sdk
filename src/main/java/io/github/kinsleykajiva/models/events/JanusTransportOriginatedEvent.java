package io.github.kinsleykajiva.models.events;

import io.github.kinsleykajiva.Janus;
import io.github.kinsleykajiva.cache.DatabaseConnection;
import io.github.kinsleykajiva.cache.mongodb.MongoConnection;
import io.github.kinsleykajiva.cache.mysql.MySqlConnection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.kinsleykajiva.models.events.JanusCoreEvent.getDatabaseConnectionListMap;

/**
 * Represents a Janus transport-originated event. This class contains records for the root event,
 * the event itself, and the associated data.
 */
public class JanusTransportOriginatedEvent {

  /**
   * The root record for a Janus transport-originated event, containing information about the
   * emitter, event type, timestamp, and the nested event details.
   *
   * @param emitter The emitter of the Janus transport-originated event.
   * @param type The type of the Janus transport-originated event.
   * @param timestamp The timestamp of the Janus transport-originated event.
   * @param event The nested event details of the Janus transport-originated event.
   */
  public record Root(String emitter, Integer type, Long timestamp, Event event) {}

  /**
   * The record representing an event originated from Janus transport, including details about the
   * transport, event ID, and associated data.
   *
   * @param transport The transport details of the Janus transport-originated event.
   * @param id The event ID of the Janus transport-originated event.
   * @param data The associated data of the Janus transport-originated event.
   */
  public record Event(String transport, String id, Data data) {}

  /**
   * The record representing data associated with a Janus transport-originated event, including the
   * event name, admin API status, IP address, and port.
   *
   * @param event The name of the event.
   * @param admin_api The admin API status.
   * @param ip The IP address.
   * @param port The port.
   */
  public record Data(String event, Boolean admin_api, String ip, Integer port) {}

  /**
   * Inserts the Janus transport-originated event into the database.
   *
   * @param root The root event containing the emitter, event type, timestamp, and event details.
   * @return The SQL query for inserting the event into the database.
   * @see #trackInsert(Root)
   */
  public Map<DatabaseConnection, List<String>> trackInsert(Root root) {
    Map<DatabaseConnection, List<String>> map = new HashMap<>();
    String emitter = root.emitter();
    int type = root.type();
    var timestamp = new Timestamp(root.timestamp() / 1000);
    Event event = root.event();

    String transport = event.transport();
    String id = event.id();
    Data data = event.data();

    String eventName = data.event();
    boolean adminApi = data.admin_api();
    String ip = data.ip();
    int port = data.port();

    var sql =
        "INSERT INTO janus_transports (emitter, type, timestamp, transport, event_id, event, admin_api, ip, port) "
            + "VALUES ('"
            + emitter
            + "', "
            + type
            + ", '"
            + timestamp
            + "', '"
            + transport
            + "', '"
            + id
            + "', '"
            + eventName
            + "', '"
            + adminApi
            + "', '"
            + ip
            + "', "
            + port
            + ")";
    

    var docTransports =
        String.format(
            "{\"insert\": \"janus_transports\", \"documents\": [{\"emitter\": \"%s\", \"type\": %d, \"timestamp\": \"%s\", \"transport\": \"%s\", \"event_id\": \"%s\", \"event\": \"%s\", \"admin_api\": \"%s\", \"ip\": \"%s\", \"port\": %d}]}",
            emitter, type, timestamp, transport, id, eventName, adminApi, ip, port);
    return getDatabaseConnectionListMap(map, sql, docTransports);
  }
}
