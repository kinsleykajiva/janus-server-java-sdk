package io.github.kinsleykajiva.janus.client.handle;

import io.github.kinsleykajiva.janus.client.JanusSession;
import io.github.kinsleykajiva.janus.client.event.JanusEventListener;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class JanusHandle {
	protected final JanusSession session;
	protected final long handleId;
	protected final List<JanusEventListener> listeners = new CopyOnWriteArrayList<>();
	private final HandleType handleType;
	
	public JanusHandle(JanusSession session, long handleId, @NonNull HandleType handleType) {
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
		listeners.add(listener);
	}
	
	public void removeListener(JanusEventListener listener) {
		listeners.remove(listener);
	}
	
	public abstract void fireEvent(JSONObject event);
	
	public CompletableFuture<JSONObject> sendMessage(JSONObject body, JSONObject jsep) {
		String transactionId = session.getClient().getTransactionManager().createTransaction();
		var future = session.getClient().getTransactionManager().registerTransaction(transactionId);
		
		JSONObject message = new JSONObject();
		message.put("janus", "message");
		message.put("body", body);
		message.put("session_id", session.getSessionId());
		message.put("handle_id", handleId);
		message.put("transaction", transactionId);
		
		if (jsep != null) {
			message.put("jsep", jsep);
		}

		session.getClient().sendMessage(message);
		return future;
	}
	
	public CompletableFuture<JSONObject> sendMessage(JSONObject body) {
		return sendMessage(body, null);
	}

	public void detach() {
		JSONObject message = new JSONObject();
		message.put("janus", "detach");
		message.put("session_id", session.getSessionId());
		message.put("handle_id", handleId);
		String transactionId = session.getClient().getTransactionManager().createTransaction();
		message.put("transaction", transactionId);
	
		session.getClient().sendMessage(message);
	}
}