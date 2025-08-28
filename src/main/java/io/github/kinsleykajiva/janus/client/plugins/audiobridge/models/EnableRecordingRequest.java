package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to enable or disable recording for a room.
 *
 * @param room       The unique numeric ID of the room.
 * @param secret     The secret required to manage the room, if any.
 * @param record     Whether to enable or disable recording.
 * @param recordFile The file to record to.
 * @param recordDir  The directory to save the recording in.
 */
public record EnableRecordingRequest(
    long room,
    String secret,
    Boolean record,
    String recordFile,
    String recordDir
) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "enable_recording")
            .put("room", room);
        if (secret != null) json.put("secret", secret);
        if (record != null) json.put("record", record);
        if (recordFile != null) json.put("record_file", recordFile);
        if (recordDir != null) json.put("record_dir", recordDir);
        return json;
    }
}
