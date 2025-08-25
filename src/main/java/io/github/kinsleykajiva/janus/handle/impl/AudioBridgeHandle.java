package io.github.kinsleykajiva.janus.handle.impl;

import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.handle.HandleType;
import io.github.kinsleykajiva.janus.handle.JanusHandle;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.github.kinsleykajiva.janus.event.JanusEvent;
import io.github.kinsleykajiva.janus.event.JanusJsep;

public class AudioBridgeHandle extends JanusHandle {
	
	public AudioBridgeHandle(JanusSession session, long handleId, HandleType handleType) {
		super(session, handleId, handleType);
	}

	@Override
	public void fireEvent(JSONObject event) {
		// A basic fireEvent implementation for AudioBridge.
		// It forwards the generic onEvent to all listeners.
		// A more detailed implementation could parse audiobridge-specific events.
		JSONObject jsep = event.optJSONObject("jsep");
		JanusJsep janusJsep = jsep != null ? new JanusJsep(jsep.optString("type"), jsep.optString("sdp")) : null;
		JanusEvent janusEvent = new JanusEvent(event, janusJsep);

		listeners.forEach(listener -> listener.onEvent(janusEvent));
	}
	
	/**
	 * Creates a new AudioBridge room.
	 * @return A CompletableFuture that completes with the success response from Janus.
	 */
	public CompletableFuture<JSONObject> createRoomAsync() {
		JSONObject body = new JSONObject().put("request", "create");
		return sendMessage(body);
	}
	
	/**
	 * Joins an AudioBridge room. This is an asynchronous action in Janus.
	 * The CompletableFuture completes when the 'joined' event is received.
	 * @param roomId The numeric ID of the room to join.
	 * @return A CompletableFuture that completes with the 'joined' event data.
	 */
	public CompletableFuture<JSONObject> joinRoomAsync(long roomId) {
		var future = new CompletableFuture<JSONObject>();
		// We listen for the specific "joined" event for this action.
		addListener(event -> {
			JSONObject pluginData = event.eventData().optJSONObject("plugindata");
			if (pluginData != null) {
				JSONObject data = pluginData.optJSONObject("data");
				if (data != null && "joined".equals(data.optString("audiobridge"))) {
					future.complete(data);
				}
			}
		});
		
		JSONObject body = new JSONObject()
				                  .put("request", "join")
				                  .put("room", roomId);
		
		// We send the message but rely on the listener to complete the future.
		sendMessage(body);
		
		return future;
	}
	
	public JSONObject joinRoomBlocking(long roomId, long timeoutMillis) throws TimeoutException {
		try {
			return joinRoomAsync(roomId).get(timeoutMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | java.util.concurrent.ExecutionException e) {
			throw new RuntimeException("Failed to join room", e);
		}
	}
	
	/**
	 * Configures your stream in the room (e.g., mute/unmute).
	 * @param muted true to mute, false to unmute.
	 * @return A CompletableFuture with the success response.
	 */
	public CompletableFuture<JSONObject> configureAsync(boolean muted) {
		JSONObject body = new JSONObject()
				                  .put("request", "configure")
				                  .put("muted", muted);
		return sendMessage(body);
	}
	
	public CompletableFuture<JSONObject> listParticipantsAsync(long roomId) {
		JSONObject body = new JSONObject()
				                  .put("request", "listparticipants")
				                  .put("room", roomId);
		return sendMessage(body);
	}
}