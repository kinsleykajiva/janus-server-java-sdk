package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents a subscription to a publisher's stream.
 */
public class Subscription {
    private final JSONObject json = new JSONObject();

    /**
     * Creates a new Subscription object.
     * @param feed       The unique ID of the publisher to subscribe to.
     * @param mid        The unique mid of the publisher's stream to subscribe to (optional).
     * @param crossrefid An ID to map this subscription with entries in the streams list (optional).
     */
    public Subscription(long feed, String mid, String crossrefid) {
        json.put("feed", feed);
        if (mid != null) {
            json.put("mid", mid);
        }
        if (crossrefid != null) {
            json.put("crossrefid", crossrefid);
        }
    }

    /**
     * Gets the JSON representation of the subscription.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
