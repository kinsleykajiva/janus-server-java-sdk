package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents the successful response from an 'edit' room request.
 *
 * @param room The unique numeric ID of the room that was edited.
 */
public record EditRoomResponse(long room) {

    /**
     * Creates an {@link EditRoomResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link EditRoomResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful 'edited' event.
     */
    public static EditRoomResponse fromJson(JSONObject json) {
        if (json == null || !"edited".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for EditRoomResponse: " + json);
        }
        return new EditRoomResponse(json.getLong("room"));
    }
}
