package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record QueryEventHandler(String transaction, String handler, JSONObject request) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "query_eventhandler");
		json.put("transaction", transaction);
		json.put("handler", handler);
		json.put("request", request);
		return json;
	}
}
