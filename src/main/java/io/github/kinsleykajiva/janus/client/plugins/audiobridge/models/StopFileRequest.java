package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to stop playing a file in a room.
 *
 * @param room   The unique numeric ID of the room.
 * @param secret The secret required to manage the room, if any.
 * @param fileId The unique ID of the playback to stop.
 */
public record StopFileRequest(long room, String secret, String fileId) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "stop_file")
            .put("room", room)
            .put("file_id", fileId);
        if (secret != null) json.put("secret", secret);
        return json;
    }
}
