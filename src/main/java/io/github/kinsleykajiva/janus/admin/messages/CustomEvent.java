package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class CustomEvent {
    private final String transaction;
    private final JSONObject event;

    public CustomEvent(String transaction, JSONObject event) {
        this.transaction = transaction;
        this.event = event;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "custom_event");
        json.put("transaction", transaction);
        json.put("event", event);
        return json;
    }
}
