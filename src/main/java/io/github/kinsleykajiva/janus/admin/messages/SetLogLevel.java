package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class SetLogLevel {
    private final String transaction;
    private final int level;

    public SetLogLevel(String transaction, int level) {
        this.transaction = transaction;
        this.level = level;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "set_log_level");
        json.put("transaction", transaction);
        json.put("level", level);
        return json;
    }
}
