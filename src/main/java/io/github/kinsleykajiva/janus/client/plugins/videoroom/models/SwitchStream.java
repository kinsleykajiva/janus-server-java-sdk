package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A helper class that defines a single stream switch operation within a `switch` request.
 */
public class SwitchStream {
    private final JSONObject json = new JSONObject();

    /**
     * Creates a new SwitchStream object.
     * @param feed   The unique ID of the publisher to switch to.
     * @param mid    The mid of the stream from the new publisher.
     * @param subMid The mid of the subscriber's m-line that should be updated.
     */
    public SwitchStream(long feed, String mid, String subMid) {
        json.put("feed", feed);
        json.put("mid", mid);
        json.put("sub_mid", subMid);
    }

    /**
     * Gets the JSON representation of the switch definition.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
