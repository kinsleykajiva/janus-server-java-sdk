package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListHandlesResponse {
    private final List<Long> handleIds;

    public ListHandlesResponse(JSONObject json) {
        JSONArray handlesArray = json.getJSONArray("handles");
        this.handleIds = new ArrayList<>();
        for (int i = 0; i < handlesArray.length(); i++) {
            this.handleIds.add(handlesArray.getLong(i));
        }
    }

    public List<Long> getHandleIds() {
        return handleIds;
    }
}
