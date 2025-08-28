package io.github.kinsleykajiva.janus.client.plugins.videoroom.events;

import org.json.JSONObject;

/**
 * An event indicating that a video room has been destroyed.
 *
 * @param room The unique numeric ID of the room that was destroyed.
 */
public record RoomDestroyedEvent(long room) {

    /**
     * Creates a {@link RoomDestroyedEvent} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link RoomDestroyedEvent} instance.
     * @throws IllegalArgumentException if the JSON does not represent a valid 'destroyed' event.
     */
    public static RoomDestroyedEvent fromJson(JSONObject json) {
        if (json == null || !"destroyed".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for RoomDestroyedEvent: " + json);
        }
        return new RoomDestroyedEvent(json.getLong("room"));
    }
}
