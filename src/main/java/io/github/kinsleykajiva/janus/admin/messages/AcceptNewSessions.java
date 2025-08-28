package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record AcceptNewSessions(String transaction, boolean accept) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "accept_new_sessions");
		json.put("transaction", transaction);
		json.put("accept", accept);
		return json;
	}
}
