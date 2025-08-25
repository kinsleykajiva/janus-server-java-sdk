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
							
						case "missed_call":
							listener.onMissedCallEvent(new JanusSipEvents.MissedCallEvent(
									result.getString("caller"),
									result.getString("displayname"),
									result.getString("callee")
							
							));
							break;
						case "message":
							listener.onMessageEvent(new JanusSipEvents.MessageEvent(
									result.getString("sender"),
									result.getString("displayname"),
									result.getString("content_type"),
									result.getString("content"),
									result.getJSONObject("headers")
							
							));
							break;
						case "info":
							listener.onInfoEvent(new JanusSipEvents.InfoEvent(
									result.getString("sender"),
									result.getString("displayname"),
									result.getString("type"),
									result.getString("content"),
									result.getJSONObject("headers")
							
							));
							break;
						case "notify":
							listener.onNotifyEvent(new JanusSipEvents.NotifyEvent(
									result.getString("notify"),
									result.getString("substate"),
									result.getString("content-type"),
									result.getString("content"),
									result.getJSONObject("headers")
							
							));
							break;
						case "transfer":
							listener.onTransferEvent(new JanusSipEvents.TransferEvent(
									result.getString("refer_id"),
									result.getString("refer_to"),
									result.getString("referred_by"),
									result.getString("replaces"),
									result.getJSONObject("headers")
							
							));
							break;
						case "messagedelivery":
							listener.onMessageDeliveryEvent(new JanusSipEvents.MessageDeliveryEvent(
									data.getString("call_id"),
									result.getInt("code"),
									result.getString("reason")
							
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
	
	/**
	 * Unregisters from the SIP server.
	 *
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> unregister() {
		final var body = new JSONObject().put("request", "unregister");
		return sendMessage(body);
	}

	/**
	 * Declines an incoming call.
	 *
	 * @param code    The SIP code to be sent (e.g., 486 for Busy Here). If not set, 486 is used.
	 * @param headers A {@link JSONObject} containing custom headers to add to the SIP request.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> decline(final int code, final JSONObject headers) {
		final var body = new JSONObject().put("request", "decline");
		if (code > 0) {
			body.put("code", code);
		}
		if (headers != null && !headers.isEmpty()) {
			body.put("headers", headers);
		}
		return sendMessage(body);
	}

	/**
	 * Hangs up an ongoing call.
	 *
	 * @param headers A {@link JSONObject} containing custom headers to add to the SIP BYE.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> hangup(final JSONObject headers) {
		final var body = new JSONObject().put("request", "hangup");
		if (headers != null && !headers.isEmpty()) {
			body.put("headers", headers);
		}
		return sendMessage(body);
	}

	/**
	 * Sends a "progress" notification for an incoming call.
	 * This typically sends a 183 Session Progress response.
	 *
	 * @param jsep    The JSEP answer or offer.
	 * @param srtp    The SRTP capabilities.
	 * @param headers Custom headers to add to the SIP OK.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> progress(final JSONObject jsep, final String srtp, final JSONObject headers) {
		final var body = new JSONObject().put("request", "progress");
		if (srtp != null && !srtp.isEmpty()) {
			body.put("srtp", srtp);
		}
		if (headers != null && !headers.isEmpty()) {
			body.put("headers", headers);
		}
		return sendMessage(body, jsep);
	}

	/**
	 * Accepts an incoming call.
	 *
	 * @param jsep    The JSEP answer or offer.
	 * @param srtp    The SRTP capabilities.
	 * @param headers Custom headers to add to the SIP OK.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> accept(final JSONObject jsep, final String srtp, final JSONObject headers) {
		final var body = new JSONObject().put("request", "accept");
		if (srtp != null && !srtp.isEmpty()) {
			body.put("srtp", srtp);
		}
		if (headers != null && !headers.isEmpty()) {
			body.put("headers", headers);
		}
		return sendMessage(body, jsep);
	}

	/**
	 * Puts the call on hold.
	 *
	 * @param direction The media direction (e.g., "sendonly", "recvonly", "inactive").
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> hold(final String direction) {
		final var body = new JSONObject().put("request", "hold");
		if (direction != null && !direction.isEmpty()) {
			body.put("direction", direction);
		}
		return sendMessage(body);
	}

	/**
	 * Resumes a call that was on hold.
	 *
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> unhold() {
		final var body = new JSONObject().put("request", "unhold");
		return sendMessage(body);
	}

	/**
	 * Updates an existing session (e.g., for renegotiation or ICE restart).
	 *
	 * @param offer The new JSEP offer.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> update(final JSONObject offer) {
		final var body = new JSONObject().put("request", "update");
		return sendMessage(body, offer);
	}

	/**
	 * Sends DTMF tones using SIP INFO.
	 *
	 * @param tones The DTMF tones to send.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> dtmf(final String tones) {
		final var body = new JSONObject()
				                 .put("request", "info")
				                 .put("type", "application/dtmf-relay")
				                 .put("content", "Signal=" + tones + "\\r\\nDuration=100");
		return sendMessage(body);
	}

	/**
	 * Sends a SIP INFO message.
	 *
	 * @param type    The content type of the message.
	 * @param content The content of the message.
	 * @param headers Custom headers to add to the SIP INFO.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> info(final String type, final String content, final JSONObject headers) {
		final var body = new JSONObject().put("request", "info");
		if (type != null && !type.isEmpty()) {
			body.put("type", type);
		}
		if (content != null && !content.isEmpty()) {
			body.put("content", content);
		}
		if (headers != null && !headers.isEmpty()) {
			body.put("headers", headers);
		}
		return sendMessage(body);
	}

	/**
	 * Sends a SIP MESSAGE.
	 *
	 * @param content     The text to send.
	 * @param contentType The content type.
	 * @param uri         The SIP URI of the peer (for out-of-dialog messages).
	 * @param headers     Custom headers to add to the SIP MESSAGE.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> message(final String content, final String contentType, final String uri, final JSONObject headers) {
		final var body = new JSONObject().put("request", "message").put("content", content);
		if (contentType != null && !contentType.isEmpty()) {
			body.put("content_type", contentType);
		}
		if (uri != null && !uri.isEmpty()) {
			body.put("uri", uri);
		}
		if (headers != null && !headers.isEmpty()) {
			body.put("headers", headers);
		}
		return sendMessage(body);
	}

	/**
	 * Subscribes to SIP events.
	 *
	 * @param event        The event to subscribe to (e.g., 'message-summary').
	 * @param accept       What should be put in the Accept header.
	 * @param to           Who the SUBSCRIBE should be addressed to.
	 * @param subscribeTtl Number of seconds after which the subscription should expire.
	 * @param headers      Custom headers to add to the SIP SUBSCRIBE.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> subscribe(final String event, final String accept, final String to, final int subscribeTtl, final JSONObject headers) {
		final var body = new JSONObject().put("request", "subscribe").put("event", event);
		if (accept != null && !accept.isEmpty()) {
			body.put("accept", accept);
		}
		if (to != null && !to.isEmpty()) {
			body.put("to", to);
		}
		if (subscribeTtl > 0) {
			body.put("subscribe_ttl", subscribeTtl);
		}
		if (headers != null && !headers.isEmpty()) {
			body.put("headers", headers);
		}
		return sendMessage(body);
	}

	/**
	 * Unsubscribes from SIP events.
	 *
	 * @param event   The event to unsubscribe from.
	 * @param accept  What should be put in the Accept header.
	 * @param to      Who the SUBSCRIBE should be addressed to.
	 * @param headers Custom headers to add to the SIP SUBSCRIBE.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> unsubscribe(final String event, final String accept, final String to, final JSONObject headers) {
		return subscribe(event, accept, to, 0, headers);
	}

	/**
	 * Transfers a call to a different URI.
	 *
	 * @param uri     The SIP URI to transfer the call to.
	 * @param replace The Call-ID of the call to replace (for attended transfers).
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> transfer(final String uri, final String replace) {
		final var body = new JSONObject().put("request", "transfer").put("uri", uri);
		if (replace != null && !replace.isEmpty()) {
			body.put("replace", replace);
		}
		return sendMessage(body);
	}

	/**
	 * Starts or stops recording a call.
	 *
	 * @param action       The action to perform ("start" or "stop").
	 * @param audio        Whether to record our audio.
	 * @param video        Whether to record our video.
	 * @param peerAudio    Whether to record our peer's audio.
	 * @param peerVideo    Whether to record our peer's video.
	 * @param sendPeerPli  Whether to send a PLI to request a keyframe from the peer.
	 * @param filename     The base path/filename to use for the recordings.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> recording(final String action, final boolean audio, final boolean video, final boolean peerAudio, final boolean peerVideo, final boolean sendPeerPli, final String filename) {
		final var body = new JSONObject()
				                 .put("request", "recording")
				                 .put("action", action)
				                 .put("audio", audio)
				                 .put("video", video)
				                 .put("peer_audio", peerAudio)
				                 .put("peer_video", peerVideo)
				                 .put("send_peer_pli", sendPeerPli);
		if (filename != null && !filename.isEmpty()) {
			body.put("filename", filename);
		}
		return sendMessage(body);
	}

	/**
	 * Requests a keyframe from the WebRTC user or the SIP peer.
	 *
	 * @param user Whether to send a keyframe request to the WebRTC user.
	 * @param peer Whether to send a keyframe request to the SIP peer.
	 * @return A {@link CompletableFuture} that resolves with the server's response.
	 */
	public CompletableFuture<JSONObject> keyframe(final boolean user, final boolean peer) {
		final var body = new JSONObject()
				                 .put("request", "keyframe")
				                 .put("user", user)
				                 .put("peer", peer);
		return sendMessage(body);
	}
}