package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to resume a suspended participant in a room.
 *
 * @param room          The unique numeric ID of the room.
 * @param secret        The secret required to manage the room, if any.
 * @param participantId The unique numeric ID of the participant to resume.
 * @param record        Whether to record the participant's contribution.
 * @param filename      The filename to use for the recording.
 */
public record ResumeParticipantRequest(long room, String secret, long participantId, Boolean record, String filename) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "resume")
            .put("room", room)
            .put("id", participantId)
            .put("secret", secret);
        if (record != null) json.put("record", record);
        if (filename != null) json.put("filename", filename);
        return json;
    }
}
