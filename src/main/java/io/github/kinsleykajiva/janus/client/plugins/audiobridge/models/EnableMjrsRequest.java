package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to enable or disable MJR recording for a room.
 *
 * @param room    The unique numeric ID of the room.
 * @param secret  The secret required to manage the room, if any.
 * @param mjrs    Whether to enable or disable MJR recording.
 * @param mjrsDir The directory to save the MJR files in.
 */
public record EnableMjrsRequest(
    long room,
    String secret,
    Boolean mjrs,
    String mjrsDir
) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "enable_mjrs")
            .put("room", room);
        if (secret != null) json.put("secret", secret);
        if (mjrs != null) json.put("mjrs", mjrs);
        if (mjrsDir != null) json.put("mjrs_dir", mjrsDir);
        return json;
    }
}
