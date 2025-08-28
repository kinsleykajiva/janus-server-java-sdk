package io.github.kinsleykajiva.janus.client.plugins.videoroom.events;

import org.json.JSONObject;

/**
 * An event indicating that a participant has left the room.
 *
 * @param room    The unique numeric ID of the room where the event occurred.
 * @param leaving The unique numeric ID of the participant who left.
 */
public record ParticipantLeftEvent(long room, long leaving) {

    /**
     * Creates a {@link ParticipantLeftEvent} instance from a {@link JSONObject}.
     * This event is identified by the presence of a 'leaving' field within a generic 'event' message.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link ParticipantLeftEvent} instance.
     * @throws IllegalArgumentException if the JSON is null or does not contain a 'leaving' field.
     */
    public static ParticipantLeftEvent fromJson(JSONObject json) {
        if (json == null || !json.has("leaving")) {
            throw new IllegalArgumentException("Invalid JSON for ParticipantLeftEvent: " + json);
        }
        long leavingId = json.getLong("leaving");
        return new ParticipantLeftEvent(
            json.getLong("room"),
            leavingId
        );
    }
}
