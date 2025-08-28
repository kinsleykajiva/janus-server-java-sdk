package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A helper record representing a single active remotization for a publisher.
 * This is part of the response to a `list_remotes` request.
 *
 * @param remoteId  The unique ID of this remotization.
 * @param host      The address all RTP packets are being sent to.
 * @param port      The port all RTP packets are being sent to.
 * @param rtcpPort  The RTCP port, if enabled.
 */
public record RemoteRemotization(String remoteId, String host, int port, int rtcpPort) {

    /**
     * Creates a {@link RemoteRemotization} instance from a {@link JSONObject}.
     *
     * @param json The JSON object representing the remotization.
     * @return A new {@link RemoteRemotization} instance.
     */
    public static RemoteRemotization fromJson(JSONObject json) {
        return new RemoteRemotization(
            json.getString("remote_id"),
            json.getString("host"),
            json.getInt("port"),
            json.optInt("rtcp_port")
        );
    }
}
