package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to stop an RTP forward stream.
 *
 * @param room     The unique numeric ID of the room.
 * @param streamId The ID of the RTP stream to stop.
 */
public record StopRtpForwardRequest(long room, long streamId) {
    public JSONObject toJson() {
        return new JSONObject()
            .put("request", "stop_rtp_forward")
            .put("room", room)
            .put("stream_id", streamId);
    }
}
