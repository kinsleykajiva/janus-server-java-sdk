package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to destroy an existing video room.
 */
public class DestroyRoomRequest {
    private final JSONObject json;

    /**
     * Creates a new DestroyRoomRequest.
     * @param room      The unique numeric ID of the room to destroy.
     * @param secret    The room secret, if required to destroy the room.
     * @param permanent Whether to remove the room from the configuration file if it's static.
     */
    public DestroyRoomRequest(long room, String secret, Boolean permanent) {
        this.json = new JSONObject();
        json.put("request", "destroy");
        json.put("room", room);
        if (secret != null) {
            json.put("secret", secret);
        }
        if (permanent != null) {
            json.put("permanent", permanent);
        }
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
