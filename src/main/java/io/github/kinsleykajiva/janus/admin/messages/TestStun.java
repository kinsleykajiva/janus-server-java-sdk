package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record TestStun(String transaction) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "test_stun");
		json.put("transaction", transaction);
		return json;
	}
}
