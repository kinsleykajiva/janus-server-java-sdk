package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record DetachHandle(String transaction, long sessionId, long handleId) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "detach_handle");
		json.put("transaction", transaction);
		json.put("session_id", sessionId);
		json.put("handle_id", handleId);
		return json;
	}
}
