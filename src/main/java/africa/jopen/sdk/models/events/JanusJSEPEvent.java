package africa.jopen.sdk.models.events;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;

/**
 * Represents a Janus JSEP event, containing information about the emitter,
 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
 */
public class JanusJSEPEvent {
	
	/**
	 * The root record for a Janus JSEP event, containing information about the emitter,
	 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
	 *
	 * @param emitter The emitter of the event.
	 * @param type The type of the event.
	 * @param timestamp The timestamp of the event.
	 * @param session_id The session ID of the event.
	 * @param handle_id The handle ID of the event.
	 * @param opaque_id The opaque ID of the event.
	 * @param event The nested event details.
	 *
	 */
	public record Root(@NotNull String emitter,@NotNull  int type,@NotNull   long timestamp,@NotNull   long session_id,@NotNull   long handle_id,@NotNull   String opaque_id,@NotNull   JanusJSEPEvent.Event event) {
	}
	
	/**
	 * The record representing a JSEP event within Janus, including the owner and the nested JSEP details.
	 */
	public record Event(String owner,@Nullable JanusJSEPEvent.Jsep jsep) {
	}
	
	/**
	 * The record representing the JSEP details associated with a Janus JSEP event,
	 * including the type (e.g., offer, answer) and the SDP (Session Description Protocol) content.
	 *
	 * @param type The type of the JSEP event.
	 * @param sdp The SDP content of the JSEP event.
	 */
	public record Jsep(@Nullable String type,@Nullable  String sdp) {
	}
	
	
	/**
	 * SQL statement to insert a Janus JSEP event into the database.
	* @param root The root record for a Janus JSEP event.
	* */
	public String trackInsert(@NotNull JanusJSEPEvent.Root root){
		var timestamp = new Timestamp(root.timestamp() / 1000);
    return String.format(
        "INSERT INTO janus_sdps (session, handle, remote, offer, sdp, timestamp) VALUES (%d, %d, %b, %b, '%s', FROM_UNIXTIME(%d))",
        root.session_id(), root.handle_id(), root.event().jsep().type().equals("offer"), root.event().jsep().type().equals("answer"), root.event().jsep().sdp(), timestamp
    );
}
}

