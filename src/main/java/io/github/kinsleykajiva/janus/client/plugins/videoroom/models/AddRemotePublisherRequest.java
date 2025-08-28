package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A request to add a new remote publisher to a room for room cascading.
 */
public class AddRemotePublisherRequest {
    private final JSONObject json;

    private AddRemotePublisherRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "add_remote_publisher");
        json.put("room", builder.room);
        if (builder.id != null) json.put("id", builder.id);
        if (builder.secret != null) json.put("secret", builder.secret);
        if (builder.display != null) json.put("display", builder.display);
        if (builder.mcast != null) json.put("mcast", builder.mcast);
        if (builder.iface != null) json.put("iface", builder.iface);
        if (builder.port != null) json.put("port", builder.port);
        if (builder.srtpSuite != null) json.put("srtp_suite", builder.srtpSuite);
        if (builder.srtpCrypto != null) json.put("srtp_crypto", builder.srtpCrypto);
        if (builder.streams != null && !builder.streams.isEmpty()) {
            json.put("streams", builder.streams.stream().map(RemoteStream::toJson).collect(Collectors.toList()));
        }
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link AddRemotePublisherRequest} instances.
     */
    public static class Builder {
        private final long room;
        private Long id;
        private String secret;
        private String display;
        private String mcast;
        private String iface;
        private Integer port;
        private Integer srtpSuite;
        private String srtpCrypto;
        private List<RemoteStream> streams;

        public Builder(long room) {
            this.room = room;
        }

        public Builder setId(long id) { this.id = id; return this; }
        public Builder setSecret(String secret) { this.secret = secret; return this; }
        public Builder setDisplay(String display) { this.display = display; return this; }
        public Builder setMcast(String mcast) { this.mcast = mcast; return this; }
        public Builder setIface(String iface) { this.iface = iface; return this; }
        public Builder setPort(int port) { this.port = port; return this; }
        public Builder setSrtpSuite(int srtpSuite) { this.srtpSuite = srtpSuite; return this; }
        public Builder setSrtpCrypto(String srtpCrypto) { this.srtpCrypto = srtpCrypto; return this; }
        public Builder setStreams(List<RemoteStream> streams) { this.streams = streams; return this; }

        public AddRemotePublisherRequest build() {
            return new AddRemotePublisherRequest(this);
        }
    }
}
