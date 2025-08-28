package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public record HandleInfoResponse(JSONObject info) {
	public HandleInfoResponse(JSONObject info) {
		this.info = info.getJSONObject("info");
	}
}
