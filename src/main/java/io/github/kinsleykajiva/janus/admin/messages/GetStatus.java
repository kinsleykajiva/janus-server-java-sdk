package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record GetStatus(String transaction) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "get_status");
		json.put("transaction", transaction);
		return json;
	}
}
