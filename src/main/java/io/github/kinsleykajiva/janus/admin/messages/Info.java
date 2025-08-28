package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record Info(String transaction) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "info");
		json.put("transaction", transaction);
		return json;
	}
}
