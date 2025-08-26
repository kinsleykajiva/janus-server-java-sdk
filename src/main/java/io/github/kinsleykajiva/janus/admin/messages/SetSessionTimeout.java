package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class SetSessionTimeout {
    private final String transaction;
    private final int timeout;

    public SetSessionTimeout(String transaction, int timeout) {
        this.transaction = transaction;
        this.timeout = timeout;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "set_session_timeout");
        json.put("transaction", transaction);
        json.put("timeout", timeout);
        return json;
    }
}
