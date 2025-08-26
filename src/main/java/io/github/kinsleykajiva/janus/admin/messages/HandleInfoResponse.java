package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class HandleInfoResponse {
    private final JSONObject info;

    public HandleInfoResponse(JSONObject json) {
        this.info = json.getJSONObject("info");
    }

    public JSONObject getInfo() {
        return info;
    }
}
