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
	public record Root(String emitter, int type, int subtype, long timestamp, Event event) {
	}
	
	/**
	 * The record representing a core event within Janus, including the status information
	 * and additional details provided by the nested Info record.
	 *
	 * @param status The status of the event.
	 * @param info The additional information associated with the event.
	 */
	public record Event(String status, Info info) {
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
	public  Map<DatabaseConnection, List<String >> trackInsert( Root root ) {
		Map<DatabaseConnection, List<String >> map       = new HashMap<>();
		var                                    timestamp = new Timestamp(root.timestamp() / 1000);
		var sql= String.format(
				"INSERT INTO janus_core (name, value, timestamp) VALUES ('%s', '%s', '%s');",
				root.emitter(), root.event().status(), timestamp
		);
		var docCore = String.format(
				"{insert: 'janus_core', documents: [{name: '%s', value: '%s', timestamp: '%s'}]}",
				root.emitter(), root.event().status(), timestamp
		);
		Arrays.asList(Janus.DB_ACCESS.getDatabaseConnections()).forEach(databaseConnection -> {
			if (databaseConnection instanceof MySqlConnection) {
				map.put(databaseConnection, List.of(sql));
			}
			if (databaseConnection instanceof MongoConnection) {
				map.put(databaseConnection, List.of(docCore));
			}
		});
		return map;
	}
}
