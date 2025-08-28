package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A helper class to define updates for a specific stream in a subscriber's `configure` request.
 */
public class SubscriberStreamUpdate {
    private final JSONObject json = new JSONObject();

    private SubscriberStreamUpdate(Builder builder) {
        json.put("mid", builder.mid);
        if (builder.send != null) json.put("send", builder.send);
        if (builder.substream != null) json.put("substream", builder.substream);
        if (builder.temporal != null) json.put("temporal", builder.temporal);
        if (builder.fallback != null) json.put("fallback", builder.fallback);
        if (builder.spatialLayer != null) json.put("spatial_layer", builder.spatialLayer);
        if (builder.temporalLayer != null) json.put("temporal_layer", builder.temporalLayer);
        if (builder.audioLevelAverage != null) json.put("audio_level_average", builder.audioLevelAverage);
        if (builder.audioActivePackets != null) json.put("audio_active_packets", builder.audioActivePackets);
        if (builder.minDelay != null) json.put("min_delay", builder.minDelay);
        if (builder.maxDelay != null) json.put("max_delay", builder.maxDelay);
    }

    /**
     * Gets the JSON representation of the stream update.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link SubscriberStreamUpdate} instances.
     */
    public static class Builder {
        private final String mid;
        private Boolean send;
        private Integer substream;
        private Integer temporal;
        private Integer fallback;
        private Integer spatialLayer;
        private Integer temporalLayer;
        private Integer audioLevelAverage;
        private Integer audioActivePackets;
        private Integer minDelay;
        private Integer maxDelay;

        /**
         * Creates a new builder for a SubscriberStreamUpdate.
         * @param mid The mid of the m-line to refer to.
         */
        public Builder(String mid) {
            this.mid = mid;
        }

        public Builder setSend(boolean send) {
            this.send = send;
            return this;
        }

        public Builder setSubstream(int substream) {
            this.substream = substream;
            return this;
        }

        public Builder setTemporal(int temporal) {
            this.temporal = temporal;
            return this;
        }

        public Builder setFallback(int fallback) {
            this.fallback = fallback;
            return this;
        }

        public Builder setSpatialLayer(int spatialLayer) {
            this.spatialLayer = spatialLayer;
            return this;
        }

        public Builder setTemporalLayer(int temporalLayer) {
            this.temporalLayer = temporalLayer;
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

        public Builder setMinDelay(int minDelay) {
            this.minDelay = minDelay;
            return this;
        }

        public Builder setMaxDelay(int maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        public SubscriberStreamUpdate build() {
            return new SubscriberStreamUpdate(this);
        }
    }
}
