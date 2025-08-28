package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to mute a room.
 *
 * @param room   The unique numeric ID of the room.
 * @param secret The secret required to manage the room, if any.
 */
public record MuteRoomRequest(long room, String secret) {
    public JSONObject toJson() {
        return new JSONObject()
            .put("request", "mute_room")
            .put("room", room)
            .put("secret", secret);
    }
}
