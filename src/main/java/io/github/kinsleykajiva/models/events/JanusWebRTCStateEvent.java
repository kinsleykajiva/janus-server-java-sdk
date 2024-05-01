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

public class JanusWebRTCStateEvent {

  /**
   * The root record for a Janus WebRTC state event, containing information about the emitter, event
   * type, subtype, timestamp, session ID, handle ID, opaque ID, and the nested event details.
   *
   * @param emitter The emitter of the Janus WebRTC state event.
   * @param type The type of the Janus WebRTC state event.
   * @param subtype The subtype of the Janus WebRTC state event.
   * @param timestamp The timestamp of the Janus WebRTC state event.
   * @param session_id The session ID of the Janus WebRTC state event.
   * @param handle_id The handle ID of the Janus WebRTC state event.
   * @param opaque_id The opaque ID of the Janus WebRTC state event.
   * @param event The nested event details of the Janus WebRTC state event.
   */
  public record Root(
      String emitter,
      int type,
      int subtype,
      long timestamp,
      long session_id,
      long handle_id,
      String opaque_id,
      Event event) {}

  /**
   * The record representing a WebRTC state event within Janus, including ICE (Interactive
   * Connectivity Establishment) details, stream ID, and component ID.
   *
   * @param ice The ICE (Interactive Connectivity Establishment) details of the WebRTC state event.
   * @param stream_id The stream ID of the WebRTC state event.
   * @param component_id The component ID of the WebRTC state event.
   * @param local_candidate The local candidate of the WebRTC state event.
   * @param remote_candidate The remote candidate of the WebRTC state event.
   */
  public record Event(
      String ice,
      int stream_id,
      int component_id,
      String local_candidate,
      String remote_candidate) {}

  /**
   * Returns the SQL INSERT statement for inserting a JanusWebRTCStateEvent into the janus_ice
   * table.
   *
   * @param root the JanusWebRTCStateEvent.Root object containing the data to be inserted
   * @return the SQL INSERT statement
   */
  public Map<DatabaseConnection, List<String>> trackInsert(Root root) {
    Map<DatabaseConnection, List<String>> map = new HashMap<>();
    var timestamp = new Timestamp(root.timestamp() / 1000);
    //

    var sql =
        "INSERT INTO janus_ice (session, handle, stream, component, state, timestamp,local_candidate,remote_candidate) VALUES ("
            + root.session_id()
            + ", "
            + root.handle_id()
            + ", "
            + root.event().stream_id()
            + ", "
            + root.event().component_id()
            + ", '"
            + root.event().ice()
            + "', '"
            + timestamp
            + "' , '"
            + root.event().local_candidate()
            + "','"
            + root.event().remote_candidate()
            + "'); ";

    /*var doc = String.format(
    		"{'insert': '%s', 'documents': [{'session': %d, 'handle': %d, 'stream': %d, 'component': %d, 'state': '%s', 'timestamp': '%s', 'local_candidate': '%s', 'remote_candidate': '%s'}]}",
    		"janus_ice",
    		root.session_id(),
    		root.handle_id(),
    		root.event().stream_id(),
    		root.event().component_id(),
    		root.event().ice(),
    		timestamp,
    		root.event().local_candidate(),
    		root.event().remote_candidate()
    );*/
    var doc =
        String.format(
            "{\"insert\": \"%s\", \"documents\": [{\"session\": %d, \"handle\": %d, \"stream\": %d, \"component\": %d, \"state\": \"%s\", \"timestamp\": \"%s\", \"local_candidate\": \"%s\", \"remote_candidate\": \"%s\"}]}",
            "janus_ice",
            root.session_id(),
            root.handle_id(),
            root.event().stream_id(),
            root.event().component_id(),
            root.event().ice(),
            timestamp,
            root.event().local_candidate(),
            root.event().remote_candidate() == null
                ? null
                : root.event().remote_candidate().trim());
    return getDatabaseConnectionListMap(map, sql, doc);
  }
}
