package io.github.kinsleykajiva.janus.handle;

import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.event.*;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class JanusHandle {
	protected final JanusSession session;
	protected final long handleId;
	private final List<JanusEventListener> listeners = new CopyOnWriteArrayList<>();
	private final List<JanusSipEventListener> sipListeners = new CopyOnWriteArrayList<>();
	private HandleType handleType;
	
	public JanusHandle(JanusSession session, long handleId,@NonNull HandleType handleType) {
		this.session = session;
		this.handleId = handleId;
		this.handleType = handleType;
	}
	
	public HandleType getHandleType() {
		return handleType;
	}
	
	public long getHandleId() {
		return handleId;
	}
	
	public JanusSession getSession() {
		return session;
	}
	
	public void addListener(JanusEventListener listener) {
		if (handleType == HandleType.SIP && listener instanceof JanusSipEventListener sipListener) {
			sipListeners.add(sipListener);
		} else if (handleType != HandleType.SIP) {
			listeners.add(listener);
		} else {
			throw new IllegalArgumentException("SIP handles require JanusSipEventListener");
		}
	}
	
	public void removeListener(JanusEventListener listener) {
		if (handleType == HandleType.SIP && listener instanceof JanusSipEventListener sipListener) {
			sipListeners.remove(sipListener);
		} else if (handleType != HandleType.SIP) {
			listeners.remove(listener);
		}
	}
	
	public void fireEvent(JSONObject event, HandleType handleType) {
		if (handleType.equals(HandleType.AUDIO_BRIDGE) && !event.has("jsep")) {
			return;
		}
		if (handleType.equals(HandleType.SIP)) {
			JSONObject jsep = event.optJSONObject("jsep");
			JanusJsep janusJsep = jsep != null ? new JanusJsep(jsep.optString("type"), jsep.optString("sdp")) : null;
			JanusEvent janusEvent = new JanusEvent(event, janusJsep);
			for (JanusSipEventListener listener : sipListeners) {
				listener.onEvent(janusEvent);
				// Optionally, dispatch to more specific SIP event methods if event type matches
				JSONObject plugindata = event.optJSONObject("plugindata");
				if (plugindata != null) {
					JSONObject data = plugindata.optJSONObject("data");
					if (data != null) {
						String eventType = data.getJSONObject("result").optString("event");
						if ("registered".equals(eventType)) {
							listener.onRegisteredEvent(janusEvent);
						} else if ("incomingcall".equals(eventType)) {
							
							listener.onIncomingCallEvent(new JanusSipEvents.InComingCallEvent(
							data.getJSONObject("result").getString("username"),
							data.getJSONObject("result").getString("call_id"),
							data.getJSONObject("result").getString("displayname"),
							data.getJSONObject("result").getString("callee"),
									janusJsep
							));
						} else if ("hangup".equals(eventType)) {
							listener.onHangupCallEvent(
									new JanusSipEvents.HangupEvent(
											data.getJSONObject("result").getInt("code"),
											data.getJSONObject("result").getString("reason"),
											data.getString("call_id")
									)
									
							);
						}
					}
				}
			}
		} else {
			JanusEvent janusEvent = new JanusEvent(event, null);
			listeners.forEach(listener -> listener.onEvent(janusEvent));
		}
	}
	
	public CompletableFuture<JSONObject> sendMessage(JSONObject body) {
		String transactionId = session.getClient().getTransactionManager().createTransaction();
		var future = session.getClient().getTransactionManager().registerTransaction(transactionId);
		
		JSONObject message = new JSONObject();
		message.put("janus", "message");
		message.put("body", body);
		message.put("session_id", session.getSessionId());
		message.put("handle_id", handleId);
		message.put("transaction", transactionId);
		
		session.getClient().sendMessage(message);
		return future;
	}
	
	public void detach() {
		// Implementation for detaching the handle from Janus
	}
}