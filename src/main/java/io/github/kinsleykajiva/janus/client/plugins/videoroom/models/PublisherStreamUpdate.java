package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A helper class to define updates for a specific stream in a publisher's `configure` request.
 */
public class PublisherStreamUpdate {
    private final JSONObject json = new JSONObject();

    private PublisherStreamUpdate(Builder builder) {
        json.put("mid", builder.mid);
        if (builder.keyframe != null) json.put("keyframe", builder.keyframe);
        if (builder.send != null) json.put("send", builder.send);
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
     * A builder for creating {@link PublisherStreamUpdate} instances.
     */
    public static class Builder {
        private final String mid;
        private Boolean keyframe;
        private Boolean send;
        private Integer minDelay;
        private Integer maxDelay;

        /**
         * Creates a new builder for a PublisherStreamUpdate.
         * @param mid The mid of the m-line to tweak.
         */
        public Builder(String mid) {
            this.mid = mid;
        }

        public Builder setKeyframe(boolean keyframe) {
            this.keyframe = keyframe;
            return this;
        }

        public Builder setSend(boolean send) {
            this.send = send;
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

        public PublisherStreamUpdate build() {
            return new PublisherStreamUpdate(this);
        }
    }
}
