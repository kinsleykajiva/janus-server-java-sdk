package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class ListHandles {
    private final String transaction;
    private final long sessionId;

    public ListHandles(String transaction, long sessionId) {
        this.transaction = transaction;
        this.sessionId = sessionId;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "list_handles");
        json.put("transaction", transaction);
        json.put("session_id", sessionId);
        return json;
    }
}
