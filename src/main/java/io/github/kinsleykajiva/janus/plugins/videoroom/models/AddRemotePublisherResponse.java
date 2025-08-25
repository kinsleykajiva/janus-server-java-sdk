package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents the successful response from an 'add_remote_publisher' request.
 *
 * @param room     The room the remote publisher was added to.
 * @param id       The ID assigned to the new remote publisher.
 * @param ip       The host address to use to send RTP packets to this publisher.
 * @param port     The port to use to send RTP packets to this publisher.
 * @param rtcpPort The port to use for RTCP feedback from this publisher.
 */
public record AddRemotePublisherResponse(long room, long id, String ip, int port, int rtcpPort) {

    /**
     * Creates an {@link AddRemotePublisherResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link AddRemotePublisherResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful response.
     */
    public static AddRemotePublisherResponse fromJson(JSONObject json) {
        if (json == null || !"success".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for AddRemotePublisherResponse: " + json);
        }
        return new AddRemotePublisherResponse(
            json.getLong("room"),
            json.getLong("id"),
            json.getString("ip"),
            json.getInt("port"),
            json.optInt("rtcp_port") // rtcp_port is optional
        );
    }
}
