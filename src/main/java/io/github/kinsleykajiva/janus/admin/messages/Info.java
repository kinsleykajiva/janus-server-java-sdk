package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class Info {
    private final String transaction;

    public Info(String transaction) {
        this.transaction = transaction;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "info");
        json.put("transaction", transaction);
        return json;
    }
}
