package io.github.kinsleykajiva.janus.handle.impl;

import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.event.*;
import io.github.kinsleykajiva.janus.handle.HandleType;
import io.github.kinsleykajiva.janus.handle.JanusHandle;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.kinsleykajiva.janus.JanusUtils.validateIpOrDomain;

public class SipHandle extends JanusHandle {
	private String sipServer = "";
	private final List<JanusSipEventListener> sipListeners = new CopyOnWriteArrayList<>();

	public SipHandle(JanusSession session, long handleId, @NonNull HandleType handleType) {
		super(session, handleId, handleType);
	}

	@Override
	public void addListener(JanusEventListener listener) {
		if (listener instanceof JanusSipEventListener sipListener) {
			this.sipListeners.add(sipListener);
		} else {
			throw new IllegalArgumentException("SipHandle only accepts JanusSipEventListener");
		}
	}

	@Override
	public void removeListener(JanusEventListener listener) {
		if (listener instanceof JanusSipEventListener sipListener) {
			this.sipListeners.remove(sipListener);
		}
	}


	@Override
	public void fireEvent(JSONObject event) {
		JSONObject jsep = event.optJSONObject("jsep");
		JanusJsep janusJsep = jsep != null ? new JanusJsep(jsep.optString("type"), jsep.optString("sdp")) : null;
		JanusEvent janusEvent = new JanusEvent(event, janusJsep);

		for (JanusSipEventListener listener : sipListeners) {
			listener.onEvent(janusEvent); // Generic event

			// Dispatch to more specific SIP event methods
			JSONObject plugindata = event.optJSONObject("plugindata");
			if (plugindata != null) {
				JSONObject data = plugindata.optJSONObject("data");
				if (data != null && data.has("result")) {
					JSONObject result = data.getJSONObject("result");
					String eventType = result.optString("event");

					switch (eventType) {
						case "registration_failed":
							listener.onFailedRegistrationEvent(new JanusSipEvents.ErrorRegistration(
									result.optString("event"),
									result.optInt("code"),
									result.optString("reason")
							));
							break;
							
						case "registered":
							listener.onRegisteredEvent(new JanusSipEvents.SuccessfulRegistration(
									result.optString("event"),
									result.optLong("master_id"),
									result.optString("username")
							));
							break;
						case "incomingcall":
							listener.onIncomingCallEvent(new JanusSipEvents.InComingCallEvent(
									result.getString("username"),
									result.getString("call_id"),
									result.getString("displayname"),
									result.getString("callee"),
									janusJsep
							));
							break;
						case "hangup":
							listener.onHangupCallEvent(new JanusSipEvents.HangupEvent(
									result.getInt("code"),
									result.getString("reason"),
									data.getString("call_id") // 'call_id' is sibling to 'result' in hangup
							));
							break;
					}
				}
			}
		}
	}
	
	/**
	 * Registers with a SIP server asynchronously.
	 *
	 * <p>This method sends a registration request to a SIP server. It returns a `CompletableFuture`
	 * that will be resolved with a {@link JanusSipEvents.RegistrationEvent} upon receiving
	 * the 'registered' event from the Janus server. This operation is handled by a temporary,
	 * self-removing listener to prevent memory leaks.</p>
	 *
	 * @param username The SIP URI to register (e.g., "user@domain").
	 * @param secret   The password for authentication.
	 * @param server   The SIP server proxy (e.g., "sip.server.com").
	 * @return A `CompletableFuture` that resolves with a `JanusSipEvents.RegistrationEvent`.
	 */
	public CompletableFuture<JanusSipEvents.RegistrationEvent> registerAsync(String username, String secret, String server) {
		validateIpOrDomain(server);
		this.sipServer = server;
		var future = new CompletableFuture<JanusSipEvents.RegistrationEvent>();

		
		JSONObject body = new JSONObject()
				                  .put("request", "register")
				                  .put("username", "sip:" + username + "@" + server)
				                  .put("display_name", username)
				                  .put("server", server)
				                  .put("secret", secret)
				                  .put("proxy", "sip:" + server + ";transport=udp");
		
		sendMessage(body).exceptionally(ex -> {
			future.completeExceptionally(ex);
			return null;
		});

		return future;
	}
	
	/**
	 * Initiates an asynchronous SIP call to the specified phone number.
	 *
	 * <p>Under the hood, this method constructs a SIP URI in the format <code>sip:phoneNumber@sipServer</code>
	 * using the current SIP server value. It then creates a JSON body for the call request and a JSEP object
	 * containing the SDP offer required for WebRTC signaling. The message is sent to the Janus server using
	 * the <code>sendMessageWithJsep</code> helper, which handles transaction management and message dispatch.
	 * The returned <code>CompletableFuture</code> resolves with the Janus server's acknowledgment response.
	 * Note: Call progress (ringing, answered, failed, etc.) is handled asynchronously via event listeners.</p>
	 *
	 * @param phoneNumber The target SIP username or number to call (e.g., "otheruser" for sip:otheruser@domain).
	 * @param offer       The SDP offer for the WebRTC call, typically generated by the client.
	 * @return A <code>CompletableFuture</code> that resolves with the Janus server's acknowledgment response.
	 */
	public CompletableFuture<JSONObject> callAsync(String phoneNumber, JSONObject offer) {
		JSONObject body = new JSONObject()
				                  .put("request", "call")
				                  .put("uri", "sip:" + phoneNumber + "@" + sipServer);
		
		JSONObject jsep = new JSONObject()
				                  .put("type", "offer")
				                  .put("sdp", offer.getString("sdp"));
		
		return sendMessage(body, jsep);
	}
	
}