package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to leave a video room (for publishers) or close a subscription (for subscribers).
 */
public class LeaveRequest {
    private final JSONObject json = new JSONObject().put("request", "leave");

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
