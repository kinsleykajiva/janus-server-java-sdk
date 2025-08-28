package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to suspend a participant in a room.
 *
 * @param room          The unique numeric ID of the room.
 * @param secret        The secret required to manage the room, if any.
 * @param participantId The unique numeric ID of the participant to suspend.
 * @param pauseEvents   Whether to pause events for the suspended participant.
 * @param stopRecord    Whether to stop recording for the suspended participant.
 */
public record SuspendParticipantRequest(long room, String secret, long participantId, Boolean pauseEvents, Boolean stopRecord) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "suspend")
            .put("room", room)
            .put("id", participantId)
            .put("secret", secret);
        if (pauseEvents != null) json.put("pause_events", pauseEvents);
        if (stopRecord != null) json.put("stop_record", stopRecord);
        return json;
    }
}
