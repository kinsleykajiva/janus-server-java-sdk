package io.github.kinsleykajiva.janus.client.plugins.audiobridge.events;

import org.json.JSONObject;

/**
 * An event indicating that an AudioBridge room has been destroyed.
 *
 * @param roomId The ID of the room that was destroyed.
 */
public record RoomDestroyedEvent(long roomId) {

    /**
     * Creates a {@link RoomDestroyedEvent} from a {@link JSONObject}.
     *
     * @param json The JSON object from Janus.
     * @return A new instance of {@link RoomDestroyedEvent}.
     */
    public static RoomDestroyedEvent fromJson(JSONObject json) {
        return new RoomDestroyedEvent(json.getLong("room"));
    }
}
