package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to check if an AudioBridge room exists.
 *
 * @param room The unique numeric ID of the room to check.
 */
public record ExistsRequest(long room) {
    public JSONObject toJson() {
        return new JSONObject().put("request", "exists").put("room", room);
    }
}
