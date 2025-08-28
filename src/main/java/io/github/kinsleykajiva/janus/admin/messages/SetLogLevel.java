package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record SetLogLevel(String transaction, int level) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "set_log_level");
		json.put("transaction", transaction);
		json.put("level", level);
		return json;
	}
}
