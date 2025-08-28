package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to stop all file playbacks in a room.
 *
 * @param room   The unique numeric ID of the room.
 * @param secret The secret required to manage the room, if any.
 */
public record StopAllFilesRequest(long room, String secret) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "stop_all_files")
            .put("room", room);
        if (secret != null) json.put("secret", secret);
        return json;
    }
}
