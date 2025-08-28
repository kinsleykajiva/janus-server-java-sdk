package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record SetSessionTimeout(String transaction, int timeout) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "set_session_timeout");
		json.put("transaction", transaction);
		json.put("timeout", timeout);
		return json;
	}
}
