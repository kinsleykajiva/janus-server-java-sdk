package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record ListSessions(String transaction) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "list_sessions");
		json.put("transaction", transaction);
		return json;
	}
}
