package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents the successful response from a 'create' room request.
 *
 * @param room      The unique numeric ID of the newly created room.
 * @param permanent Whether the room was saved to the config file.
 */
public record CreateRoomResponse(long room, boolean permanent) {

    /**
     * Creates a {@link CreateRoomResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link CreateRoomResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful 'created' event.
     */
    public static CreateRoomResponse fromJson(JSONObject json) {
        if (json == null || !"created".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for CreateRoomResponse: " + json);
        }
        return new CreateRoomResponse(
            json.getLong("room"),
            json.getBoolean("permanent")
        );
    }
}
