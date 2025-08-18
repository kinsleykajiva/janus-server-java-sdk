package io.github.kinsleykajiva.janus.handle.impl;

import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.handle.JanusHandle;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

public class SipHandle extends JanusHandle {
	
	public SipHandle(JanusSession session, long handleId) {
		super(session, handleId);
	}
	
	/**
	 * Registers with a SIP server. This is an async action.
	 * @param username The SIP URI to register (e.g., sip:user@domain).
	 * @param secret The password for authentication.
	 * @param server The SIP server proxy (e.g., sip:sip.server.com).
	 * @return A CompletableFuture that resolves on the 'registered' event.
	 */
	public CompletableFuture<JSONObject> registerAsync(String username, String secret, String server) {
		var future = new CompletableFuture<JSONObject>();
		addListener(event -> {
			JSONObject result = event.eventData()
					                    .optJSONObject("plugindata", new JSONObject())
					                    .optJSONObject("data", new JSONObject())
					                    .optJSONObject("result", new JSONObject());
			
			if ("registered".equals(result.optString("event"))) {
				future.complete(result);
			}
		});
		
		JSONObject body = new JSONObject()
				                  .put("request", "register")
				                  .put("username", username)
				                  .put("secret", secret)
				                  .put("proxy", server);
		
		sendMessage(body);
		return future;
	}
	
	/**
	 * Places a call to a SIP URI.
	 * @param uri The SIP URI to call (e.g., sip:otheruser@domain).
	 * @param offer The SDP offer for the WebRTC call.
	 * @return A CompletableFuture that resolves with the Janus ack. The call progress is handled via events.
	 */
	public CompletableFuture<JSONObject> callAsync(String uri, JSONObject offer) {
		JSONObject body = new JSONObject()
				                  .put("request", "call")
				                  .put("uri", uri);
		
		JSONObject jsep = new JSONObject()
				                  .put("type", "offer")
				                  .put("sdp", offer.getString("sdp"));
		
		return sendMessageWithJsep(body, jsep);
	}
	
	// A helper method for messages containing JSEP (SDP)
	private CompletableFuture<JSONObject> sendMessageWithJsep(JSONObject body, JSONObject jsep) {
		String transactionId = session.getClient().getTransactionManager().createTransaction();
		var future = session.getClient().getTransactionManager().registerTransaction(transactionId);
		
		JSONObject message = new JSONObject();
		message.put("janus", "message");
		message.put("body", body);
		message.put("jsep", jsep); // Add JSEP object here
		message.put("session_id", session.getSessionId());
		message.put("handle_id", handleId);
		message.put("transaction", transactionId);
		
		session.getClient().sendMessage(message);
		return future;
	}
}