package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A response to an exists request.
 *
 * @param room   The unique numeric ID of the room.
 * @param exists Whether the room exists.
 */
public record ExistsResponse(long room, boolean exists) {
    public static ExistsResponse fromJson(JSONObject json) {
        return new ExistsResponse(json.getLong("room"), json.getBoolean("exists"));
    }
}
