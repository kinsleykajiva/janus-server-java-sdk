package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to unmute a participant in a room.
 *
 * @param room          The unique numeric ID of the room.
 * @param secret        The secret required to manage the room, if any.
 * @param participantId The unique numeric ID of the participant to unmute.
 */
public record UnmuteParticipantRequest(long room, String secret, long participantId) {
    public JSONObject toJson() {
        return new JSONObject()
            .put("request", "unmute")
            .put("room", room)
            .put("id", participantId)
            .put("secret", secret);
    }
}
