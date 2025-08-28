package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request from a subscriber to start receiving media. This is sent after receiving
 * the JSEP offer from the plugin and is accompanied by a JSEP answer.
 */
public class StartSubscriptionRequest {
    private final JSONObject json = new JSONObject().put("request", "start");

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
