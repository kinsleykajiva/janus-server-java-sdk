package io.github.kinsleykajiva.janus.client.plugins.videoroom.events;

import io.github.kinsleykajiva.janus.client.plugins.videoroom.models.Publisher;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An event indicating that one or more new publishers have become active in the room.
 *
 * @param room       The unique numeric ID of the room where the event occurred.
 * @param publishers A list containing the details of the new publishers.
 */
public record PublisherAddedEvent(long room, List<Publisher> publishers) {

    /**
     * Creates a {@link PublisherAddedEvent} instance from a {@link JSONObject}.
     * This event is identified by the presence of a 'publishers' array within a generic 'event' message.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link PublisherAddedEvent} instance.
     * @throws IllegalArgumentException if the JSON is null or does not contain a 'publishers' array.
     */
    public static PublisherAddedEvent fromJson(JSONObject json) {
         if (json == null || !json.has("publishers")) {
            throw new IllegalArgumentException("Invalid JSON for PublisherAddedEvent: " + json);
        }
        var publishersArray = json.getJSONArray("publishers");
        List<Publisher> publisherList = IntStream.range(0, publishersArray.length())
            .mapToObj(publishersArray::getJSONObject)
            .map(Publisher::fromJson)
            .collect(Collectors.toList());

        return new PublisherAddedEvent(
            json.getLong("room"),
            publisherList
        );
    }
}
