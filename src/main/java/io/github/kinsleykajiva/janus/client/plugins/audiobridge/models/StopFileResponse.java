package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A response to a stop_file request.
 *
 * @param room   The unique numeric ID of the room.
 * @param fileId The unique ID of the playback that was stopped.
 */
public record StopFileResponse(long room, String fileId) {
    public static StopFileResponse fromJson(JSONObject json) {
        return new StopFileResponse(json.getLong("room"), json.getString("file_id"));
    }
}
