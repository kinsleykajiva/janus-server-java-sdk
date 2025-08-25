package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to get a list of all available video rooms.
 */
public class ListRoomsRequest {
    private final JSONObject json = new JSONObject().put("request", "list");

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
