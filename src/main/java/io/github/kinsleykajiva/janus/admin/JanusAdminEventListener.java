package io.github.kinsleykajiva.janus.admin;

import org.json.JSONObject;

@FunctionalInterface
public interface JanusAdminEventListener {
    void onEvent(JSONObject event);
}
