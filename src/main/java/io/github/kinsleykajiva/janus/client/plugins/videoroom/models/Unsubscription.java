package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A helper class to define a stream to unsubscribe from in an `update` request.
 */
public class Unsubscription {
    private final JSONObject json = new JSONObject();

    private Unsubscription(Builder builder) {
        if (builder.feed != null) json.put("feed", builder.feed);
        if (builder.mid != null) json.put("mid", builder.mid);
        if (builder.subMid != null) json.put("sub_mid", builder.subMid);
    }

    /**
     * Gets the JSON representation of the unsubscription definition.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link Unsubscription} instances.
     */
    public static class Builder {
        private Long feed;
        private String mid;
        private String subMid;

        public Builder setFeed(long feed) {
            this.feed = feed;
            return this;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder setSubMid(String subMid) {
            this.subMid = subMid;
            return this;
        }

        /**
         * Builds the {@link Unsubscription} object.
         * @return a new instance of {@link Unsubscription}.
         * @throws IllegalStateException if no property has been set.
         */
        public Unsubscription build() {
            if (feed == null && mid == null && subMid == null) {
                throw new IllegalStateException("At least one of feed, mid, or sub_mid must be specified for unsubscription.");
            }
            return new Unsubscription(this);
        }
    }
}
