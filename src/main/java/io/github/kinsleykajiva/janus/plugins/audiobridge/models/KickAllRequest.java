package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to kick all participants from a room.
 *
 * @param room   The unique numeric ID of the room.
 * @param secret The secret required to manage the room, if any.
 */
public record KickAllRequest(long room, String secret) {
    public JSONObject toJson() {
        return new JSONObject()
            .put("request", "kick_all")
            .put("room", room)
            .put("secret", secret);
    }
}
