package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class DetachHandle {
    private final String transaction;
    private final long sessionId;
    private final long handleId;

    public DetachHandle(String transaction, long sessionId, long handleId) {
        this.transaction = transaction;
        this.sessionId = sessionId;
        this.handleId = handleId;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "detach_handle");
        json.put("transaction", transaction);
        json.put("session_id", sessionId);
        json.put("handle_id", handleId);
        return json;
    }
}
