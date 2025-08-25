package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request from a publisher to stop publishing their media stream.
 */
public class UnpublishRequest {
    private final JSONObject json = new JSONObject().put("request", "unpublish");

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
