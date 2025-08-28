package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents the successful response from a 'destroy' room request.
 *
 * @param room The unique numeric ID of the room that was destroyed.
 */
public record DestroyRoomResponse(long room) {

    /**
     * Creates a {@link DestroyRoomResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link DestroyRoomResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful 'destroyed' event.
     */
    public static DestroyRoomResponse fromJson(JSONObject json) {
        if (json == null || !"destroyed".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for DestroyRoomResponse: " + json);
        }
        return new DestroyRoomResponse(json.getLong("room"));
    }
}
