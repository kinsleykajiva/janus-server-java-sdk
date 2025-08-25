package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to start relaying a local publisher's media to a remote Janus instance.
 */
public class PublishRemotelyRequest {
    private final JSONObject json;

    private PublishRemotelyRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "publish_remotely");
        json.put("room", builder.room);
        json.put("publisher_id", builder.publisherId);
        json.put("remote_id", builder.remoteId);
        json.put("host", builder.host);
        json.put("port", builder.port);
        if (builder.hostFamily != null) json.put("host_family", builder.hostFamily);
        if (builder.rtcpPort != null) json.put("rtcp_port", builder.rtcpPort);
        if (builder.srtpSuite != null) json.put("srtp_suite", builder.srtpSuite);
        if (builder.srtpCrypto != null) json.put("srtp_crypto", builder.srtpCrypto);
        if (builder.secret != null) json.put("secret", builder.secret);
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link PublishRemotelyRequest} instances.
     */
    public static class Builder {
        private final long room;
        private final long publisherId;
        private final String remoteId;
        private final String host;
        private final int port;
        private String hostFamily;
        private Integer rtcpPort;
        private Integer srtpSuite;
        private String srtpCrypto;
        private String secret;

        public Builder(long room, long publisherId, String remoteId, String host, int port) {
            this.room = room;
            this.publisherId = publisherId;
            this.remoteId = remoteId;
            this.host = host;
            this.port = port;
        }

        public Builder setHostFamily(String hostFamily) { this.hostFamily = hostFamily; return this; }
        public Builder setRtcpPort(int rtcpPort) { this.rtcpPort = rtcpPort; return this; }
        public Builder setSrtpSuite(int srtpSuite) { this.srtpSuite = srtpSuite; return this; }
        public Builder setSrtpCrypto(String srtpCrypto) { this.srtpCrypto = srtpCrypto; return this; }
        public Builder setSecret(String secret) { this.secret = secret; return this; }

        public PublishRemotelyRequest build() {
            return new PublishRemotelyRequest(this);
        }
    }
}
