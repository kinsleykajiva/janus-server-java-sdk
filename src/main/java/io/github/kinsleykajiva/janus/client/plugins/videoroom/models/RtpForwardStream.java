package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A helper class to define a single stream to be forwarded in an RTP forward request.
 */
public class RtpForwardStream {
    private final JSONObject json = new JSONObject();

    private RtpForwardStream(Builder builder) {
        json.put("mid", builder.mid);
        json.put("port", builder.port);
        if (builder.host != null) json.put("host", builder.host);
        if (builder.ssrc != null) json.put("ssrc", builder.ssrc);
        if (builder.pt != null) json.put("pt", builder.pt);
        if (builder.rtcpPort != null) json.put("rtcp_port", builder.rtcpPort);
        if (builder.simulcast != null) json.put("simulcast", builder.simulcast);
        if (builder.port2 != null) json.put("port_2", builder.port2);
        if (builder.ssrc2 != null) json.put("ssrc_2", builder.ssrc2);
        if (builder.pt2 != null) json.put("pt_2", builder.pt2);
        if (builder.port3 != null) json.put("port_3", builder.port3);
        if (builder.ssrc3 != null) json.put("ssrc_3", builder.ssrc3);
        if (builder.pt3 != null) json.put("pt_3", builder.pt3);
    }

    /**
     * Gets the JSON representation of the stream definition.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link RtpForwardStream} instances.
     */
    public static class Builder {
        private final String mid;
        private final int port;
        private String host;
        private Integer ssrc;
        private Integer pt;
        private Integer rtcpPort;
        private Boolean simulcast;
        private Integer port2;
        private Integer ssrc2;
        private Integer pt2;
        private Integer port3;
        private Integer ssrc3;
        private Integer pt3;

        /**
         * Creates a new builder for an RtpForwardStream.
         * @param mid The mid of the publisher stream to forward.
         * @param port The port to forward the packets to.
         */
        public Builder(String mid, int port) {
            this.mid = mid;
            this.port = port;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setSsrc(int ssrc) {
            this.ssrc = ssrc;
            return this;
        }

        public Builder setPt(int pt) {
            this.pt = pt;
            return this;
        }

        public Builder setRtcpPort(int rtcpPort) {
            this.rtcpPort = rtcpPort;
            return this;
        }

        public Builder setSimulcast(boolean simulcast) {
            this.simulcast = simulcast;
            return this;
        }

        public Builder setPort2(int port2) {
            this.port2 = port2;
            return this;
        }

        public Builder setSsrc2(int ssrc2) {
            this.ssrc2 = ssrc2;
            return this;
        }

        public Builder setPt2(int pt2) {
            this.pt2 = pt2;
            return this;
        }

        public Builder setPort3(int port3) {
            this.port3 = port3;
            return this;
        }

        public Builder setSsrc3(int ssrc3) {
            this.ssrc3 = ssrc3;
            return this;
        }

        public Builder setPt3(int pt3) {
            this.pt3 = pt3;
            return this;
        }

        public RtpForwardStream build() {
            return new RtpForwardStream(this);
        }
    }
}
