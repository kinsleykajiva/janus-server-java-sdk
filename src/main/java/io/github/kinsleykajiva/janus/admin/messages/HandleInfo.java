package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class HandleInfo {
    private final String transaction;
    private final long sessionId;
    private final long handleId;

    public HandleInfo(String transaction, long sessionId, long handleId) {
        this.transaction = transaction;
        this.sessionId = sessionId;
        this.handleId = handleId;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "handle_info");
        json.put("transaction", transaction);
        json.put("session_id", sessionId);
        json.put("handle_id", handleId);
        return json;
    }
}
