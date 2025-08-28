package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record Ping(String transaction) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "ping");
		json.put("transaction", transaction);
		return json;
	}
}
