package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to unmute a room.
 *
 * @param room   The unique numeric ID of the room.
 * @param secret The secret required to manage the room, if any.
 */
public record UnmuteRoomRequest(long room, String secret) {
    public JSONObject toJson() {
        return new JSONObject()
            .put("request", "unmute_room")
            .put("room", room)
            .put("secret", secret);
    }
}
