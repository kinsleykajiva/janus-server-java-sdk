package io.github.kinsleykajiva.models.events;


import io.github.kinsleykajiva.Janus;
import io.github.kinsleykajiva.cache.DatabaseConnection;
import io.github.kinsleykajiva.cache.mongodb.MongoConnection;
import io.github.kinsleykajiva.cache.mysql.MySqlConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.*;

/**
 * Represents a Janus JSEP event, containing information about the emitter,
 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
 */
public class JanusJSEPEvent {
	
	/**
	 * The root record for a Janus JSEP event, containing information about the emitter,
	 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
	 *
	 * @param emitter    The emitter of the event.
	 * @param type       The type of the event.
	 * @param timestamp  The timestamp of the event.
	 * @param session_id The session ID of the event.
	 * @param handle_id  The handle ID of the event.
	 * @param opaque_id  The opaque ID of the event.
	 * @param event      The nested event details.
	 */
	public record Root(@NotNull String emitter, @NotNull int type, @NotNull long timestamp, @NotNull long session_id, @NotNull long handle_id, @NotNull String opaque_id, @NotNull JanusJSEPEvent.Event event) {
	}
	
	/**
	 * The record representing a JSEP event within Janus, including the owner and the nested JSEP details.
	 */
	public record Event(String owner, @Nullable JanusJSEPEvent.Jsep jsep) {
	}
	
	/**
	 * The record representing the JSEP details associated with a Janus JSEP event,
	 * including the type (e.g., offer, answer) and the SDP (Session Description Protocol) content.
	 *
	 * @param type The type of the JSEP event.
	 * @param sdp  The SDP content of the JSEP event.
	 */
	public record Jsep(@Nullable String type, @Nullable String sdp) {
	}
	
	
	/**
	 * Inserts a Janus JSEP event into the appropriate database(s).
	 *
	 * @param root The root record for a Janus JSEP event.
	 * @return A map containing the SQL or MongoDB command strings for each database connection.
	 */
	public Map<DatabaseConnection, List<String >> trackInsert( @NotNull JanusJSEPEvent.Root root ) {
		Map<DatabaseConnection, List<String >> map       = new HashMap<>();
		
		var                             timestamp = new Timestamp(root.timestamp() / 1000);
		
		if (root.event().jsep() != null) {
			if (root.event().jsep().type() != null) {
				List<String> sqlList = new ArrayList<>();
				List<String> docList = new ArrayList<>();
				var sql = String.format(
						"INSERT INTO janus_sdps (session, handle, remote, offer, sdp, timestamp) VALUES (%d, %d, %b, %b, '%s', FROM_UNIXTIME(%d))",
						root.session_id(), root.handle_id(), root.event().jsep().type().equals("offer"), root.event().jsep().type().equals("answer"), root.event().jsep().sdp(), timestamp
				);
				sqlList.add(sql);
				
				/*var doc = String.format(
						"{'insert': '%s', 'documents': [{'session': %d, 'handle': %d, 'remote': %b, 'offer': %b, 'sdp': '%s', 'timestamp': %tc}]}",
						"janus_sdps",
						root.session_id(), root.handle_id(), root.event().jsep().type().equals("offer"), root.event().jsep().type().equals("answer"), root.event().jsep().sdp(), timestamp
				);*/
				var json = new JSONObject()
						.put("insert", "janus_sdps")
						.put("documents",
								new JSONObject()
										.put("session", root.session_id())
										.put("handle",root.handle_id())
										.put("remote", root.event().jsep().type().equals("offer"))
										.put("offer", root.event().jsep().type().equals("answer"))
										.put("sdp", root.event().jsep().sdp())
									
										.put("timestamp", timestamp)
						).toString();
				var doc = String.format(
						"{\"insert\": \"%s\", \"documents\": [{\"session\": %d, \"handle\": %d, \"remote\": %b, \"offer\": %b, \"sdp\": \"%s\", \"timestamp\": \"%tc\"}]}",
						"janus_sdps",
						root.session_id(), root.handle_id(), root.event().jsep().type().equals("offer"), root.event().jsep().type().equals("answer"), root.event().jsep().sdp(), timestamp
				);
				docList.add(doc);
				
				Arrays.asList(Janus.DB_ACCESS.getDatabaseConnections()).forEach(databaseConnection -> {
					if (databaseConnection instanceof MySqlConnection) {
						map.put(databaseConnection, sqlList);
					}
					if (databaseConnection instanceof MongoConnection) {
						map.put(databaseConnection, docList);
					}
				});
			}
		}
		return map;
	}
}

