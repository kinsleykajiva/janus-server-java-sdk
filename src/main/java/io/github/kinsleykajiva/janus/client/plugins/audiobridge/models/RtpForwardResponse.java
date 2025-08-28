package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A response to an rtp_forward request.
 *
 * @param room     The unique numeric ID of the room.
 * @param group    The group being forwarded.
 * @param streamId The ID of the RTP stream.
 * @param host     The host the stream is being forwarded to.
 * @param port     The port the stream is being forwarded to.
 */
public record RtpForwardResponse(long room, String group, long streamId, String host, int port) {
    public static RtpForwardResponse fromJson(JSONObject json) {
        return new RtpForwardResponse(
            json.getLong("room"),
            json.optString("group", null),
            json.getLong("stream_id"),
            json.getString("host"),
            json.getInt("port")
        );
    }
}
