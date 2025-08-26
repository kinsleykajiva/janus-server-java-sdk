package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class DestroySession {
    private final String transaction;
    private final long sessionId;

    public DestroySession(String transaction, long sessionId) {
        this.transaction = transaction;
        this.sessionId = sessionId;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "destroy_session");
        json.put("transaction", transaction);
        json.put("session_id", sessionId);
        return json;
    }
}
