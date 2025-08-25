package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;

/**
 * A request to forward a publisher's media streams to a remote RTP listener.
 */
public class RtpForwardRequest {
    private final JSONObject json;

    private RtpForwardRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "rtp_forward");
        json.put("room", builder.room);
        json.put("publisher_id", builder.publisherId);
        json.put("host", builder.host);
        if (builder.hostFamily != null) json.put("host_family", builder.hostFamily);
        if (builder.srtpSuite != null) json.put("srtp_suite", builder.srtpSuite);
        if (builder.srtpCrypto != null) json.put("srtp_crypto", builder.srtpCrypto);
        json.put("streams", builder.streams.stream().map(RtpForwardStream::toJson).collect(Collectors.toList()));
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link RtpForwardRequest} instances.
     */
    public static class Builder {
        private final long room;
        private final long publisherId;
        private final String host;
        private final List<RtpForwardStream> streams;
        private String hostFamily;
        private Integer srtpSuite;
        private String srtpCrypto;

        /**
         * Creates a new builder for an RtpForwardRequest.
         * @param room The room where the publisher is.
         * @param publisherId The ID of the publisher to forward.
         * @param host The host address to forward the packets to (can be overridden per stream).
         * @param streams The list of streams to forward.
         */
        public Builder(long room, long publisherId, String host, List<RtpForwardStream> streams) {
            this.room = room;
            this.publisherId = publisherId;
            this.host = host;
            this.streams = streams;
        }

        public Builder setHostFamily(String hostFamily) {
            this.hostFamily = hostFamily;
            return this;
        }

        public Builder setSrtpSuite(int srtpSuite) {
            this.srtpSuite = srtpSuite;
            return this;
        }

        public Builder setSrtpCrypto(String srtpCrypto) {
            this.srtpCrypto = srtpCrypto;
            return this;
        }

        public RtpForwardRequest build() {
            return new RtpForwardRequest(this);
        }
    }
}
