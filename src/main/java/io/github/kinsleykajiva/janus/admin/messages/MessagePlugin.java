package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record MessagePlugin(String transaction, long sessionId, long handleId, JSONObject body) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "message_plugin");
		json.put("transaction", transaction);
		json.put("session_id", sessionId);
		json.put("handle_id", handleId);
		json.put("body", body);
		return json;
	}
}
