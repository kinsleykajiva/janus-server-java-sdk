package io.github.kinsleykajiva.janus.plugins.videoroom.events;

import org.json.JSONObject;

/**
 * Represents an attendee in a room who is not an active publisher.
 * This is part of the 'joined' event when `notify_joining` is enabled.
 *
 * @param id       The unique numeric ID of the attendee.
 * @param display  The display name of the attendee, if any.
 * @param metadata A JSON object containing metadata about the attendee, if any.
 */
public record Attendee(long id, String display, JSONObject metadata) {

    /**
     * Creates an {@link Attendee} instance from a {@link JSONObject}.
     *
     * @param json The JSON object representing the attendee.
     * @return A new {@link Attendee} instance, or null if the input is null.
     */
    public static Attendee fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        return new Attendee(
            json.getLong("id"),
            json.optString("display"),
            json.optJSONObject("metadata")
        );
    }
}
