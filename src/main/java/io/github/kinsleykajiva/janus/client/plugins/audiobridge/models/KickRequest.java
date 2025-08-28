package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to kick a participant from a room.
 *
 * @param room          The unique numeric ID of the room.
 * @param secret        The secret required to manage the room, if any.
 * @param participantId The unique numeric ID of the participant to kick.
 */
public record KickRequest(long room, String secret, long participantId) {
    public JSONObject toJson() {
        return new JSONObject()
            .put("request", "kick")
            .put("room", room)
            .put("id", participantId)
            .put("secret", secret);
    }
}
