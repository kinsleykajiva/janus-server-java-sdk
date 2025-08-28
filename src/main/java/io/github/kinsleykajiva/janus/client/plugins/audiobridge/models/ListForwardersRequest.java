package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to list the RTP forwarders for a room.
 *
 * @param room The unique numeric ID of the room.
 */
public record ListForwardersRequest(long room) {
    public JSONObject toJson() {
        return new JSONObject()
            .put("request", "listforwarders")
            .put("room", room);
    }
}
