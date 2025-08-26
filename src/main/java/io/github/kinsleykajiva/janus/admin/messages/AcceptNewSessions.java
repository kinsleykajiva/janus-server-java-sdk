package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class AcceptNewSessions {
    private final String transaction;
    private final boolean accept;

    public AcceptNewSessions(String transaction, boolean accept) {
        this.transaction = transaction;
        this.accept = accept;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "accept_new_sessions");
        json.put("transaction", transaction);
        json.put("accept", accept);
        return json;
    }
}
