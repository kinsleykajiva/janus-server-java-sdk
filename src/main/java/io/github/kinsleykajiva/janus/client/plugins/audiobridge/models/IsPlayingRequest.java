package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to check if a file is playing in a room.
 *
 * @param room   The unique numeric ID of the room.
 * @param secret The secret required to manage the room, if any.
 * @param fileId The unique ID of the playback to check.
 */
public record IsPlayingRequest(long room, String secret, String fileId) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "is_playing")
            .put("room", room)
            .put("file_id", fileId);
        if (secret != null) json.put("secret", secret);
        return json;
    }
}
