package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record ResolveAddress(String transaction, String address) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "resolve_address");
		json.put("transaction", transaction);
		json.put("address", address);
		return json;
	}
}
