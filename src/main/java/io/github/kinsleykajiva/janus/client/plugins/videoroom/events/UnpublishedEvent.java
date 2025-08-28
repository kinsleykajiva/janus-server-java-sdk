package io.github.kinsleykajiva.janus.client.plugins.videoroom.events;

import org.json.JSONObject;

/**
 * An event indicating that a publisher has unpublished their stream and is no longer active.
 *
 * @param room        The unique numeric ID of the room where the event occurred.
 * @param unpublished The unique numeric ID of the publisher who unpublished.
 */
public record UnpublishedEvent(long room, long unpublished) {

    /**
     * Creates an {@link UnpublishedEvent} instance from a {@link JSONObject}.
     * This event is identified by the presence of an 'unpublished' field within a generic 'event' message.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link UnpublishedEvent} instance.
     * @throws IllegalArgumentException if the JSON is null or does not contain an 'unpublished' field.
     */
    public static UnpublishedEvent fromJson(JSONObject json) {
        if (json == null || !json.has("unpublished")) {
            throw new IllegalArgumentException("Invalid JSON for UnpublishedEvent: " + json);
        }
        return new UnpublishedEvent(
            json.getLong("room"),
            json.getLong("unpublished")
        );
    }
}
