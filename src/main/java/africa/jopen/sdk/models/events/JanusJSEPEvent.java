package africa.jopen.sdk.models.events;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Janus JSEP event, containing information about the emitter,
 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
 */
public class JanusJSEPEvent {
	
	/**
	 * The root record for a Janus JSEP event, containing information about the emitter,
	 * event type, timestamp, session ID, handle ID, opaque ID, and the nested event details.
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
	 */
	public record Jsep(@Nullable String type,@Nullable  String sdp) {
	}
	
	public String trackInsert(@NotNull JanusJSEPEvent.Root root){
    return String.format(
        "INSERT INTO janus_sdps (session, handle, remote, offer, sdp, timestamp) VALUES (%d, %d, %b, %b, '%s', FROM_UNIXTIME(%d))",
        root.session_id(), root.handle_id(), root.event().jsep().type().equals("offer"), root.event().jsep().type().equals("answer"), root.event().jsep().sdp(), root.timestamp()
    );
}
}

