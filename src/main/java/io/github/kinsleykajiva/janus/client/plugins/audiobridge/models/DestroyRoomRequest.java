package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to destroy an existing AudioBridge room.
 *
 * @param room      The unique numeric ID of the room to destroy.
 * @param secret    The secret required to destroy the room, if any.
 * @param permanent Whether to remove the room from the configuration file as well.
 */
public record DestroyRoomRequest(long room, String secret, boolean permanent) {

    /**
     * Returns the request as a {@link JSONObject}.
     *
     * @return The request as a {@link JSONObject}.
     */
    public JSONObject toJson() {
        final var json = new JSONObject();
        json.put("request", "destroy");
        json.put("room", room);
        if (secret != null) {
            json.put("secret", secret);
        }
        json.put("permanent", permanent);
        return json;
    }
}
