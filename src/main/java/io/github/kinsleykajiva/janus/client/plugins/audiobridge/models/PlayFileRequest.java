package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to play a file in a room.
 *
 * @param room     The unique numeric ID of the room.
 * @param secret   The secret required to manage the room, if any.
 * @param group    The group to play the file in.
 * @param fileId   The unique ID to assign to the playback.
 * @param filename The path to the file to play.
 * @param loop     Whether to loop the file.
 */
public record PlayFileRequest(
    long room,
    String secret,
    String group,
    String fileId,
    String filename,
    Boolean loop
) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "play_file")
            .put("room", room)
            .put("filename", filename);
        if (secret != null) json.put("secret", secret);
        if (group != null) json.put("group", group);
        if (fileId != null) json.put("file_id", fileId);
        if (loop != null) json.put("loop", loop);
        return json;
    }
}
