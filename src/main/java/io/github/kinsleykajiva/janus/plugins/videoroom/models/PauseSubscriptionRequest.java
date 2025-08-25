package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request from a subscriber to temporarily pause the media delivery for their subscription.
 */
public class PauseSubscriptionRequest {
    private final JSONObject json = new JSONObject().put("request", "pause");

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
