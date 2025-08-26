package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class QueryEventHandler {
    private final String transaction;
    private final String handler;
    private final JSONObject request;

    public QueryEventHandler(String transaction, String handler, JSONObject request) {
        this.transaction = transaction;
        this.handler = handler;
        this.request = request;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "query_eventhandler");
        json.put("transaction", transaction);
        json.put("handler", handler);
        json.put("request", request);
        return json;
    }
}
