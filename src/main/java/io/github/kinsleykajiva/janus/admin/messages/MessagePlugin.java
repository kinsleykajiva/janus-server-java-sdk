package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class MessagePlugin {
    private final String transaction;
    private final long sessionId;
    private final long handleId;
    private final JSONObject body;

    public MessagePlugin(String transaction, long sessionId, long handleId, JSONObject body) {
        this.transaction = transaction;
        this.sessionId = sessionId;
        this.handleId = handleId;
        this.body = body;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "message_plugin");
        json.put("transaction", transaction);
        json.put("session_id", sessionId);
        json.put("handle_id", handleId);
        json.put("body", body);
        return json;
    }
}
