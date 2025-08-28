package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * Represents an announcement in a room.
 *
 * @param fileId   The unique ID of the announcement.
 * @param filename The name of the file being played.
 * @param playing  Whether the announcement is currently playing.
 * @param loop     Whether the announcement is set to loop.
 */
public record Announcement(String fileId, String filename, boolean playing, boolean loop) {
    public static Announcement fromJson(JSONObject json) {
        return new Announcement(
            json.getString("file_id"),
            json.getString("filename"),
            json.getBoolean("playing"),
            json.getBoolean("loop")
        );
    }
}
