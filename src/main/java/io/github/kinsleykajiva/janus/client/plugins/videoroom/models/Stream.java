package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents a media stream within a publisher's feed in a video room.
 *
 * @param type        The type of the stream (e.g., "audio", "video", "data").
 * @param mindex      The unique m-index of the stream.
 * @param mid         The unique mid of the stream.
 * @param disabled    Whether the stream is currently inactive.
 * @param codec       The codec used for the stream (e.g., "opus", "vp8").
 * @param description A text description of the stream.
 * @param moderated   Whether the stream has been moderated (e.g., muted by an admin).
 * @param simulcast   Whether the stream uses simulcast.
 * @param svc         Whether the stream uses SVC (Scalable Video Coding).
 * @param talking     Whether there is audio activity on this stream.
 */
public record Stream(
    String type,
    String mindex,
    String mid,
    boolean disabled,
    String codec,
    String description,
    boolean moderated,
    boolean simulcast,
    boolean svc,
    boolean talking) {

    /**
     * Creates a {@link Stream} instance from a {@link JSONObject}.
     *
     * @param json The JSON object representing the stream.
     * @return A new {@link Stream} instance, or null if the input is null.
     */
    public static Stream fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        // mindex and mid can be numbers in some events, so we handle them as strings.
        String mindex = json.has("mindex") ? String.valueOf(json.get("mindex")) : null;
        String mid = json.has("mid") ? String.valueOf(json.get("mid")) : null;

        return new Stream(
            json.getString("type"),
            mindex,
            mid,
            json.optBoolean("disabled", false),
            json.optString("codec"),
            json.optString("description"),
            json.optBoolean("moderated", false),
            json.optBoolean("simulcast", false),
            json.optBoolean("svc", false),
            json.optBoolean("talking", false)
        );
    }
}
