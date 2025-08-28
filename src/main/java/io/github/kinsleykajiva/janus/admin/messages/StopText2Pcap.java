package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record StopText2Pcap(String transaction, long sessionId, long handleId) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "stop_text2pcap");
		json.put("transaction", transaction);
		json.put("session_id", sessionId);
		json.put("handle_id", handleId);
		return json;
	}
}
