package io.github.kinsleykajiva.janus.client.plugins.videoroom.events;

import org.json.JSONObject;

/**
 * Represents a single stream within a subscription.
 *
 * @param mindex      The unique m-index of this stream in the subscription.
 * @param mid         The unique mid of this stream.
 * @param type        The type of the stream's media (e.g., "audio", "video").
 * @param feedId      The unique ID of the publisher originating this stream.
 * @param feedMid     The unique mid of the publisher's stream.
 * @param feedDisplay The display name of the publisher.
 * @param send        Whether the plugin is configured to relay media for this stream.
 * @param ready       Whether this stream is ready to start sending media.
 */
public record SubscriberStream(
    int mindex,
    String mid,
    String type,
    long feedId,
    String feedMid,
    String feedDisplay,
    boolean send,
    boolean ready) {

    /**
     * Creates a {@link SubscriberStream} instance from a {@link JSONObject}.
     *
     * @param json The JSON object representing the subscriber stream.
     * @return A new {@link SubscriberStream} instance, or null if the input is null.
     */
    public static SubscriberStream fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        return new SubscriberStream(
            json.getInt("mindex"),
            json.getString("mid"),
            json.getString("type"),
            json.getLong("feed_id"),
            json.optString("feed_mid"),
            json.optString("feed_display"),
            json.getBoolean("send"),
            json.getBoolean("ready")
        );
    }
}
