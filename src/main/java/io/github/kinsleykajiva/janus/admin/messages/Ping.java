package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class Ping {
    private final String transaction;

    public Ping(String transaction) {
        this.transaction = transaction;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "ping");
        json.put("transaction", transaction);
        return json;
    }
}
