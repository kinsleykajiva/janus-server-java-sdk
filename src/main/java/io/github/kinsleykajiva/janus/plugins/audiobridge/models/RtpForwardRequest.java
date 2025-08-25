package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to forward an RTP stream from a room.
 *
 * @param room       The unique numeric ID of the room.
 * @param group      The group to forward.
 * @param ssrc       The SSRC to use for the stream.
 * @param codec      The codec to use for the stream.
 * @param ptype      The payload type to use for the stream.
 * @param host       The host to forward the stream to.
 * @param hostFamily The host family to use for the stream.
 * @param port       The port to forward the stream to.
 * @param srtpSuite  The SRTP suite to use for the stream.
 * @param srtpCrypto The SRTP crypto to use for the stream.
 * @param alwaysOn   Whether to forward silence when the room is empty.
 */
public record RtpForwardRequest(
    long room,
    String group,
    Long ssrc,
    String codec,
    Integer ptype,
    String host,
    String hostFamily,
    int port,
    Integer srtpSuite,
    String srtpCrypto,
    Boolean alwaysOn
) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "rtp_forward")
            .put("room", room)
            .put("host", host)
            .put("port", port);
        if (group != null) json.put("group", group);
        if (ssrc != null) json.put("ssrc", ssrc);
        if (codec != null) json.put("codec", codec);
        if (ptype != null) json.put("ptype", ptype);
        if (hostFamily != null) json.put("host_family", hostFamily);
        if (srtpSuite != null) json.put("srtp_suite", srtpSuite);
        if (srtpCrypto != null) json.put("srtp_crypto", srtpCrypto);
        if (alwaysOn != null) json.put("always_on", alwaysOn);
        return json;
    }
}
