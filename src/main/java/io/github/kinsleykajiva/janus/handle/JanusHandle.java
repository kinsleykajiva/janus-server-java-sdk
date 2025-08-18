package io.github.kinsleykajiva.janus.handle;

import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.event.JanusEvent;
import io.github.kinsleykajiva.janus.event.JanusEventListener;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class JanusHandle {
	protected final JanusSession session;
	protected final long handleId;
	private final List<JanusEventListener> listeners = new CopyOnWriteArrayList<>();
	
	public JanusHandle(JanusSession session, long handleId) {
		this.session = session;
		this.handleId = handleId;
	}
	
	public long getHandleId() {
		return handleId;
	}
	
	public JanusSession getSession() {
		return session;
	}
	
	public void addListener(JanusEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(JanusEventListener listener) {
		listeners.remove(listener);
	}
	
	public void fireEvent(JSONObject event) {
		JSONObject jsep = event.optJSONObject("jsep");
		JanusEvent janusEvent = new JanusEvent(event, jsep);
		listeners.forEach(listener -> listener.onEvent(janusEvent));
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