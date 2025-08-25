package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A response to an is_playing request.
 *
 * @param room    The unique numeric ID of the room.
 * @param fileId  The unique ID of the playback.
 * @param playing Whether the file is playing.
 */
public record IsPlayingResponse(long room, String fileId, boolean playing) {
    public static IsPlayingResponse fromJson(JSONObject json) {
        return new IsPlayingResponse(
            json.getLong("room"),
            json.getString("file_id"),
            json.getBoolean("playing")
        );
    }
}
