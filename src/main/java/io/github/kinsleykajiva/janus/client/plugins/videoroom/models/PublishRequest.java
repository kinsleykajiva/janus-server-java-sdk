package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A request to publish a media stream to a video room.
 */
public class PublishRequest {
    private final JSONObject json;

    private PublishRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "publish");
        if (builder.audioCodec != null) json.put("audiocodec", builder.audioCodec);
        if (builder.videoCodec != null) json.put("videocodec", builder.videoCodec);
        if (builder.bitrate != null) json.put("bitrate", builder.bitrate);
        if (builder.record != null) json.put("record", builder.record);
        if (builder.filename != null) json.put("filename", builder.filename);
        if (builder.display != null) json.put("display", builder.display);
        if (builder.metadata != null) json.put("metadata", builder.metadata);
        if (builder.audioLevelAverage != null) json.put("audio_level_average", builder.audioLevelAverage);
        if (builder.audioActivePackets != null) json.put("audio_active_packets", builder.audioActivePackets);
        if (builder.descriptions != null) {
            json.put("descriptions", builder.descriptions.stream()
                .map(StreamDescription::toJson)
                .collect(Collectors.toList()));
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
     * A builder for creating {@link PublishRequest} instances.
     */
    public static class Builder {
        private String audioCodec;
        private String videoCodec;
        private Integer bitrate;
        private Boolean record;
        private String filename;
        private String display;
        private JSONObject metadata;
        private Integer audioLevelAverage;
        private Integer audioActivePackets;
        private List<StreamDescription> descriptions;

        public Builder setAudioCodec(String audioCodec) {
            this.audioCodec = audioCodec;
            return this;
        }

        public Builder setVideoCodec(String videoCodec) {
            this.videoCodec = videoCodec;
            return this;
        }

        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        public Builder setRecord(boolean record) {
            this.record = record;
            return this;
        }

        public Builder setFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder setDisplay(String display) {
            this.display = display;
            return this;
        }

        public Builder setMetadata(JSONObject metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder setAudioLevelAverage(int audioLevelAverage) {
            this.audioLevelAverage = audioLevelAverage;
            return this;
        }

        public Builder setAudioActivePackets(int audioActivePackets) {
            this.audioActivePackets = audioActivePackets;
            return this;
        }

        public Builder setDescriptions(List<StreamDescription> descriptions) {
            this.descriptions = descriptions;
            return this;
        }

        public PublishRequest build() {
            return new PublishRequest(this);
        }
    }
}
