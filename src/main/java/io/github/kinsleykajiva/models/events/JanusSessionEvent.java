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
 * The JanusSessionEvent class represents a Janus session event, including information about the
 * emitter, event type, timestamp, session ID, and the nested event details.
 */
public class JanusSessionEvent {

  /**
   * The root record for a Janus session event, containing information about the emitter, event
   * type, timestamp, session ID, and the nested event details.
   *
   * @param emitter The emitter of the Janus session event.
   * @param type The type of the Janus session event.
   * @param timestamp The timestamp of the Janus session event.
   * @param session_id The session ID of the Janus session event.
   * @param event The nested event details of the Janus session event.
   */
  public record Root(String emitter, int type, long timestamp, long session_id, Event event) {}

  /**
   * The record representing an event within a Janus session, including the event name and the
   * associated transport details.
   *
   * @param name The event name of the Janus session event.
   * @param transport The transport details of the Janus session event.
   */
  public record Event(String name, Transport transport) {}

  /**
   * The record representing the transport details of a Janus session event, including the transport
   * type and ID.
   *
   * @param transport The transport type of the Janus session event.
   * @param id The transport ID of the Janus session event.
   */
  public record Transport(String transport, long id) {}

  /**
   * Generates an SQL INSERT statement for inserting a Janus session event into the database.
   *
   * @param root The root record of the Janus session event.
   * @return The SQL/MongoDB JSON INSERT statements in Map format.
   * @see  io.github.kinsleykajiva.models.events.JanusCoreEvent#getDatabaseConnectionListMap(Map, String, String)
   */
  public Map<DatabaseConnection, List<String>> trackInsert(Root root) {
    Map<DatabaseConnection, List<String>> map = new HashMap<>();
    var timestamp = new Timestamp(root.timestamp() / 1000);
    var sql =
        String.format(
            "INSERT INTO janus_sessions (session, event, timestamp) VALUES (%d, '%s', '%s');",
            root.session_id(), root.event().name(), timestamp);
    var docSessions =
        String.format(
            "{\"insert\": \"janus_sessions\", \"documents\": [{\"session\": %d, \"event\": \"%s\", \"timestamp\": \"%s\"}]}",
            root.session_id(), root.event().name(), timestamp);
    return getDatabaseConnectionListMap(map, sql, docSessions);
  }
}
