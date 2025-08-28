package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents the successful response from an 'exists' room request.
 *
 * @param room   The unique numeric ID of the room that was checked.
 * @param exists Whether the room exists or not.
 */
public record ExistsResponse(long room, boolean exists) {

    /**
     * Creates an {@link ExistsResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link ExistsResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful 'exists' check.
     */
    public static ExistsResponse fromJson(JSONObject json) {
        if (json == null || !"success".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for ExistsResponse: " + json);
        }
        return new ExistsResponse(
            json.getLong("room"),
            json.getBoolean("exists")
        );
    }
}
