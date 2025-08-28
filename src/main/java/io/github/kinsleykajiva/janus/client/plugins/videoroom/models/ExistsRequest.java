package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to check if a video room exists.
 */
public class ExistsRequest {
    private final JSONObject json;

    /**
     * Creates a new ExistsRequest.
     * @param room The unique numeric ID of the room to check.
     */
    public ExistsRequest(long room) {
        this.json = new JSONObject();
        json.put("request", "exists");
        json.put("room", room);
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
