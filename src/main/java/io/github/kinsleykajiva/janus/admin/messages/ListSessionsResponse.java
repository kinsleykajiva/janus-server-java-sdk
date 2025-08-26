package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListSessionsResponse {
    private final List<Long> sessionIds;

    public ListSessionsResponse(JSONObject json) {
        JSONArray sessionsArray = json.getJSONArray("sessions");
        this.sessionIds = new ArrayList<>();
        for (int i = 0; i < sessionsArray.length(); i++) {
            this.sessionIds.add(sessionsArray.getLong(i));
        }
    }

    public List<Long> getSessionIds() {
        return sessionIds;
    }
}
