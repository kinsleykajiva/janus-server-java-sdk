package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * Represents a request to configure a participant's settings in an AudioBridge room.
 * This class uses a builder pattern for easy construction.
 */
public class ConfigureRequest {
    private final JSONObject json;

    private ConfigureRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "configure");
        if (builder.muted != null) json.put("muted", builder.muted);
        if (builder.display != null) json.put("display", builder.display);
        if (builder.bitrate != null) json.put("bitrate", builder.bitrate);
        if (builder.quality != null) json.put("quality", builder.quality);
        if (builder.expectedLoss != null) json.put("expected_loss", builder.expectedLoss);
        if (builder.volume != null) json.put("volume", builder.volume);
        if (builder.spatialPosition != null) json.put("spatial_position", builder.spatialPosition);
        if (builder.denoise != null) json.put("denoise", builder.denoise);
        if (builder.record != null) json.put("record", builder.record);
        if (builder.filename != null) json.put("filename", builder.filename);
        if (builder.group != null) json.put("group", builder.group);
    }

    /**
     * Returns the JSON representation of this request object.
     * @return The {@link JSONObject}.
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link ConfigureRequest} instances.
     */
    public static class Builder {
        private Boolean muted;
        private String display;
        private Integer bitrate;
        private Integer quality;
        private Integer expectedLoss;
        private Integer volume;
        private Integer spatialPosition;
        private Boolean denoise;
        private Boolean record;
        private String filename;
        private String group;

        public Builder setMuted(boolean muted) {
            this.muted = muted;
            return this;
        }

        public Builder setDisplay(String display) {
            this.display = display;
            return this;
        }

        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        public Builder setQuality(int quality) {
            this.quality = quality;
            return this;
        }

        public Builder setExpectedLoss(int expectedLoss) {
            this.expectedLoss = expectedLoss;
            return this;
        }

        public Builder setVolume(int volume) {
            this.volume = volume;
            return this;
        }

        public Builder setSpatialPosition(int spatialPosition) {
            this.spatialPosition = spatialPosition;
            return this;
        }

        public Builder setDenoise(boolean denoise) {
            this.denoise = denoise;
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

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        /**
         * Builds the {@link ConfigureRequest}.
         * @return A new instance of {@link ConfigureRequest}.
         */
        public ConfigureRequest build() {
            return new ConfigureRequest(this);
        }
    }
}
