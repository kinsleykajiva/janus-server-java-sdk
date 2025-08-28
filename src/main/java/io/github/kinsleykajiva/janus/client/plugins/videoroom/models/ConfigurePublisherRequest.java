package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A request to configure an active publisher's session (e.g., change bitrate, update streams).
 */
public class ConfigurePublisherRequest {
    private final JSONObject json;

    private ConfigurePublisherRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "configure");
        if (builder.bitrate != null) json.put("bitrate", builder.bitrate);
        if (builder.keyframe != null) json.put("keyframe", builder.keyframe);
        if (builder.record != null) json.put("record", builder.record);
        if (builder.filename != null) json.put("filename", builder.filename);
        if (builder.display != null) json.put("display", builder.display);
        if (builder.metadata != null) json.put("metadata", builder.metadata);
        if (builder.audioActivePackets != null) json.put("audio_active_packets", builder.audioActivePackets);
        if (builder.audioLevelAverage != null) json.put("audio_level_average", builder.audioLevelAverage);
        if (builder.streams != null) {
            json.put("streams", builder.streams.stream()
                .map(PublisherStreamUpdate::toJson)
                .collect(Collectors.toList()));
        }
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
     * A builder for creating {@link ConfigurePublisherRequest} instances.
     */
    public static class Builder {
        private Integer bitrate;
        private Boolean keyframe;
        private Boolean record;
        private String filename;
        private String display;
        private JSONObject metadata;
        private Integer audioActivePackets;
        private Integer audioLevelAverage;
        private List<PublisherStreamUpdate> streams;
        private List<StreamDescription> descriptions;

        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        public Builder setKeyframe(boolean keyframe) {
            this.keyframe = keyframe;
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

        public Builder setAudioActivePackets(int audioActivePackets) {
            this.audioActivePackets = audioActivePackets;
            return this;
        }

        public Builder setAudioLevelAverage(int audioLevelAverage) {
            this.audioLevelAverage = audioLevelAverage;
            return this;
        }

        public Builder setStreams(List<PublisherStreamUpdate> streams) {
            this.streams = streams;
            return this;
        }

        public Builder setDescriptions(List<StreamDescription> descriptions) {
            this.descriptions = descriptions;
            return this;
        }

        public ConfigurePublisherRequest build() {
            return new ConfigurePublisherRequest(this);
        }
    }
}
