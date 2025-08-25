package io.github.kinsleykajiva.janus;

import io.github.kinsleykajiva.janus.handle.HandleType;
import io.github.kinsleykajiva.janus.handle.JanusHandle;
import io.github.kinsleykajiva.janus.handle.impl.AudioBridgeHandle;
import io.github.kinsleykajiva.janus.handle.impl.SipHandle;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class JanusSession {
	private final JanusClient client;
	private final long sessionId;
	private final Map<Long, JanusHandle> handles = new ConcurrentHashMap<>();

	public JanusSession(JanusClient client, long sessionId) {
		this.client = client;
		this.sessionId = sessionId;
	}

	public long getSessionId() {
		return sessionId;
	}

	public <T extends JanusHandle> CompletableFuture<T> attachPlugin(
		String pluginName, BiFunction<JanusSession, Long, T> handleFactory) {

		String transactionId = client.getTransactionManager().createTransaction();
		var future = client.getTransactionManager().registerTransaction(transactionId);

		JSONObject request = new JSONObject();
		request.put("janus", "attach");
		request.put("plugin", pluginName);
		request.put("session_id", sessionId);
		request.put("transaction", transactionId);

		client.sendMessage(request);

		return future.thenApply(response -> {
			long handleId = response.getJSONObject("data").getLong("id");
			T specificHandle = handleFactory.apply(this, handleId);
			handles.put(handleId, specificHandle);
			return specificHandle;
		});
	}

	// Convenience methods for specific plugins
	public CompletableFuture<AudioBridgeHandle> attachAudioBridgePlugin() {
		return attachPlugin("janus.plugin.audiobridge",
			(session, id) -> new AudioBridgeHandle(session, id, HandleType.AUDIO_BRIDGE));
	}

	public CompletableFuture<SipHandle> attachSipPlugin() {
		return attachPlugin("janus.plugin.sip",
			(session, id) -> new SipHandle(session, id, HandleType.SIP));
	}

	public void handleEvent(JSONObject event) {
		long handleId = event.optLong("sender", -1);
		Optional.ofNullable(handles.get(handleId)).ifPresent(handle -> handle.fireEvent(event));
	}

	public void destroy() {
		handles.values().forEach(JanusHandle::detach);
		handles.clear();
		client.removeSession(sessionId);
		// A 'destroy' message to Janus could also be sent here if desired.
	}

	// Package-private for internal use by handles
	public JanusClient getClient() {
		return client;
	}
}