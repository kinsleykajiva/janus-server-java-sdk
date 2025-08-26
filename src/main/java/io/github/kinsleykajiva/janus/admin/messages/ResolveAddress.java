package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class ResolveAddress {
    private final String transaction;
    private final String address;

    public ResolveAddress(String transaction, String address) {
        this.transaction = transaction;
        this.address = address;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "resolve_address");
        json.put("transaction", transaction);
        json.put("address", address);
        return json;
    }
}
