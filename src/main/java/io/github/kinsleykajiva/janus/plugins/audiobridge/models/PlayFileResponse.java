package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A response to a play_file request.
 *
 * @param room   The unique numeric ID of the room.
 * @param fileId The unique ID of the playback.
 */
public record PlayFileResponse(long room, String fileId) {
    public static PlayFileResponse fromJson(JSONObject json) {
        return new PlayFileResponse(json.getLong("room"), json.getString("file_id"));
    }
}
