package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record StartText2Pcap(String transaction, long sessionId, long handleId, String folder, String filename) {
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("janus", "start_text2pcap");
		json.put("transaction", transaction);
		json.put("session_id", sessionId);
		json.put("handle_id", handleId);
		if (folder != null && !folder.isEmpty()) {
			json.put("folder", folder);
		}
		if (filename != null && !filename.isEmpty()) {
			json.put("filename", filename);
		}
		return json;
	}
}
