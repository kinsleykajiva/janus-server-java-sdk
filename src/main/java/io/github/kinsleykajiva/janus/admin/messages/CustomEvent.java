package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record CustomEvent(String transaction, JSONObject event) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "custom_event");
		json.put("transaction", transaction);
		json.put("event", event);
		return json;
	}
}
