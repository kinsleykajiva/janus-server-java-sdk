package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;

/**
 * A request to update an existing remote publisher (e.g., after a renegotiation).
 */
public class UpdateRemotePublisherRequest {
    private final JSONObject json;

    private UpdateRemotePublisherRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "update_remote_publisher");
        json.put("room", builder.room);
        json.put("id", builder.id);
        if (builder.secret != null) json.put("secret", builder.secret);
        if (builder.display != null) json.put("display", builder.display);
        if (builder.metadata != null) json.put("metadata", builder.metadata);
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
     * A builder for creating {@link UpdateRemotePublisherRequest} instances.
     */
    public static class Builder {
        private final long room;
        private final long id;
        private String secret;
        private String display;
        private JSONObject metadata;
        private Integer srtpSuite;
        private String srtpCrypto;
        private List<RemoteStream> streams;

        public Builder(long room, long id) {
            this.room = room;
            this.id = id;
        }

        public Builder setSecret(String secret) { this.secret = secret; return this; }
        public Builder setDisplay(String display) { this.display = display; return this; }
        public Builder setMetadata(JSONObject metadata) { this.metadata = metadata; return this; }
        public Builder setSrtpSuite(int srtpSuite) { this.srtpSuite = srtpSuite; return this; }
        public Builder setSrtpCrypto(String srtpCrypto) { this.srtpCrypto = srtpCrypto; return this; }
        public Builder setStreams(List<RemoteStream> streams) { this.streams = streams; return this; }

        public UpdateRemotePublisherRequest build() {
            return new UpdateRemotePublisherRequest(this);
        }
    }
}
