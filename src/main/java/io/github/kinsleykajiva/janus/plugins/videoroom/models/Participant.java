package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents a participant in a video room, as returned by the `listparticipants` request.
 *
 * @param id        The unique numeric ID of the participant.
 * @param display   The display name of the participant.
 * @param metadata  A JSON object containing metadata about the participant.
 * @param publisher Whether the participant is an active publisher.
 * @param talking   Whether the participant is currently talking.
 */
public record Participant(
    long id,
    String display,
    JSONObject metadata,
    boolean publisher,
    boolean talking) {

    /**
     * Creates a {@link Participant} instance from a {@link JSONObject}.
     *
     * @param json The JSON object representing the participant.
     * @return A new {@link Participant} instance, or null if the input is null.
     */
    public static Participant fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        return new Participant(
            json.getLong("id"),
            json.optString("display"),
            json.optJSONObject("metadata"),
            json.getBoolean("publisher"),
            json.optBoolean("talking", false)
        );
    }
}
