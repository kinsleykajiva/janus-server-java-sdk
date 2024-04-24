package io.github.kinsleykajiva.models.events;


import io.github.kinsleykajiva.Janus;
import io.github.kinsleykajiva.cache.DatabaseConnection;
import io.github.kinsleykajiva.cache.mongodb.MongoConnection;
import io.github.kinsleykajiva.cache.mysql.MySqlConnection;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Janus handle event, containing information about the emitter,
 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
 */
public class JanusHandleEvent {
	
	/**
	 * The root record for a Janus handle event, containing information about the emitter,
	 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
	 */
	public record Root(String emitter, int type, long timestamp, long session_id, long handle_id, String opaque_id, Event event) {
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
	public Map<DatabaseConnection, List<String >> trackInsert( Root root){
		Map<DatabaseConnection, List<String >> map       = new HashMap<>();
		if(root == null) return map;
		var timestamp = new Timestamp(root.timestamp() / 1000);
    var sql= String.format(
        "INSERT INTO janus_handles (session, handle, event, plugin, timestamp) VALUES (%d, %d, '%s', '%s', '%s' )",
        root.session_id(), root.handle_id(), root.event().name(), root.event().plugin(), timestamp
    );
		
		/*var doc = String.format(
				"{'insert': '%s', 'documents': [{'session': %d, 'handle': %d, 'event': '%s', 'plugin': '%s', 'timestamp': '%s'}]}",
				"janus_handles",
				root.session_id(),
				root.handle_id(),
				root.event().name(),
				root.event().plugin(),
				timestamp
		);*/
		var doc = String.format(
				"{\"insert\": \"%s\", \"documents\": [{\"session\": %d, \"handle\": %d, \"event\": \"%s\", \"plugin\": \"%s\", \"timestamp\": \"%s\"}]}",
				"janus_handles",
				root.session_id(),
				root.handle_id(),
				root.event().name(),
				root.event().plugin(),
				timestamp
		);
		
		Arrays.asList(Janus.DB_ACCESS.getDatabaseConnections()).forEach(databaseConnection -> {
			if (databaseConnection instanceof MySqlConnection) {
				map.put(databaseConnection, List.of(sql));
			}
			if (databaseConnection instanceof MongoConnection) {
				map.put(databaseConnection, List.of(doc));
			}
		});
		return map;
}

}
