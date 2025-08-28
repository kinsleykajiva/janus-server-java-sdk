package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record HandleInfo(String transaction, long sessionId, long handleId) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "handle_info");
		json.put("transaction", transaction);
		json.put("session_id", sessionId);
		json.put("handle_id", handleId);
		return json;
	}
}
