package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * Represents an RTP forwarder.
 *
 * @param streamId  The unique ID of the forwarder.
 * @param group     The group being forwarded.
 * @param ip        The IP address the stream is being forwarded to.
 * @param port      The port the stream is being forwarded to.
 * @param ssrc      The SSRC of the stream.
 * @param codec     The codec of the stream.
 * @param pType     The payload type of the stream.
 * @param srtp      Whether SRTP is enabled.
 * @param alwaysOn  Whether the forwarder is always on.
 */
public record RtpForwarder(
    long streamId,
    String group,
    String ip,
    int port,
    Long ssrc,
    String codec,
    Integer pType,
    boolean srtp,
    boolean alwaysOn
) {
    public static RtpForwarder fromJson(JSONObject json) {
        return new RtpForwarder(
            json.getLong("stream_id"),
            json.optString("group", null),
            json.getString("ip"),
            json.getInt("port"),
            json.has("ssrc") ? json.getLong("ssrc") : null,
            json.optString("codec", null),
            json.has("ptype") ? json.getInt("ptype") : null,
            json.getBoolean("srtp"),
            json.getBoolean("always_on")
        );
    }
}
