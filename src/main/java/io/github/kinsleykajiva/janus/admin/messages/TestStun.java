package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class TestStun {
    private final String transaction;

    public TestStun(String transaction) {
        this.transaction = transaction;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "test_stun");
        json.put("transaction", transaction);
        return json;
    }
}
