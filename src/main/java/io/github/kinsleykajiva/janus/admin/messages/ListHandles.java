package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record ListHandles(String transaction, long sessionId) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "list_handles");
		json.put("transaction", transaction);
		json.put("session_id", sessionId);
		return json;
	}
}
