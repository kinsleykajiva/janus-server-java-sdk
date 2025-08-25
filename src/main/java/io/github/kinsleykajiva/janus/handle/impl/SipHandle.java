package io.github.kinsleykajiva.janus.handle.impl;

import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.event.JanusSipEvents;
import io.github.kinsleykajiva.janus.handle.JanusHandle;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

public class SipHandle extends JanusHandle {
	
	public SipHandle(JanusSession session, long handleId) {
		super(session, handleId);
	}
	
	/**
	 * Registers with a SIP server asynchronously.
	 *
	 * <p>This method sends a registration request to a SIP server and listens for the
	 * 'registered' event. It uses a `CompletableFuture` to handle the asynchronous
	 * nature of the operation. The future resolves with a `JanusSipEvents.RegistrationEvent`
	 * when the registration is successful or encounters an error.</p>
	 *
	 * @param username The SIP URI to register (e.g., sip:user@domain).
	 * @param secret The password for authentication.
	 * @param server The SIP server proxy (e.g., sip:sip.server.com).
	 * @return A `CompletableFuture` that resolves with a `JanusSipEvents.RegistrationEvent`
	 *         when the registration process completes.
	 */
	public CompletableFuture<JanusSipEvents.RegistrationEvent> registerAsync(String username, String secret, String server) {
		var future = new CompletableFuture<JanusSipEvents.RegistrationEvent>();
		addListener(event -> {
			JSONObject result = event.eventData()
					                    .optJSONObject("plugindata", new JSONObject())
					                    .optJSONObject("data", new JSONObject())
					                    .optJSONObject("result", new JSONObject());
			//System.out.println(event.toString());
			if ("registered".equals(result.optString("event"))) {
				if(result.optString("event").equals( "registered")) {
					JanusSipEvents.SuccessfulRegistration eventSip = new JanusSipEvents.SuccessfulRegistration(
							result.optString("event"),
							result.optLong("master_id"),
							result.optString("username")
					);
					future.complete(eventSip);
				}else{
					JanusSipEvents.ErrorRegistration eventSip = new JanusSipEvents.ErrorRegistration(
							result.optString("event"),
							result.optInt("code"),
							result.optString("reason")
					);
					future.complete(eventSip);
				}
				
				
			}
		});
		
		JSONObject body = new JSONObject()
				                  .put("request", "register")
				                  .put("username", "sip:" + username + "@" + server)
				                  .put("display_name", username)
				                  .put("server", server)
				                  .put("secret", secret)
				                  .put("proxy", "sip:" + server + ";transport=udp");
		
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