package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to stop an active RTP forwarder.
 */
public class StopRtpForwardRequest {
    private final JSONObject json;

    /**
     * Creates a new StopRtpForwardRequest.
     * @param room        The room where the publisher is.
     * @param publisherId The ID of the publisher being forwarded.
     * @param streamId    The ID of the forwarder stream to stop.
     */
    public StopRtpForwardRequest(long room, long publisherId, long streamId) {
        this.json = new JSONObject();
        json.put("request", "stop_rtp_forward");
        json.put("room", room);
        json.put("publisher_id", publisherId);
        json.put("stream_id", streamId);
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
