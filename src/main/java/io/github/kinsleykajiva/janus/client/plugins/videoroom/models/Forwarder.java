package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents an active RTP forwarder for a stream.
 *
 * @param streamId       The unique numeric ID assigned to this forwarder.
 * @param type           The type of media being forwarded ("audio", "video", "data").
 * @param host           The host this forwarder is streaming to.
 * @param port           The port this forwarder is streaming to.
 * @param localRtcpPort  The local port used for RTCP feedback.
 * @param remoteRtcpPort The remote port for RTCP feedback.
 * @param ssrc           The SSRC this forwarder is using.
 * @param pt             The payload type this forwarder is using.
 * @param substream      The video substream being relayed (if any).
 * @param srtp           Whether the RTP stream is encrypted.
 */
public record Forwarder(
    long streamId,
    String type,
    String host,
    int port,
    int localRtcpPort,
    int remoteRtcpPort,
    long ssrc,
    int pt,
    int substream,
    boolean srtp
) {
    /**
     * Creates a {@link Forwarder} instance from a {@link JSONObject}.
     *
     * @param json The JSON object representing the forwarder.
     * @return A new {@link Forwarder} instance.
     */
    public static Forwarder fromJson(JSONObject json) {
        return new Forwarder(
            json.getLong("stream_id"),
            json.getString("type"),
            json.getString("host"),
            json.getInt("port"),
            json.optInt("local_rtcp_port"),
            json.optInt("remote_rtcp_port"),
            json.optLong("ssrc"),
            json.optInt("pt"),
            json.optInt("substream"),
            json.getBoolean("srtp")
        );
    }
}
