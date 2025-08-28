package io.github.kinsleykajiva.janus.client.plugins.videoroom.events;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An event indicating a subscriber handle has been successfully attached,
 * detailing the streams it is configured to receive. This event is accompanied
 * by a JSEP offer from the plugin.
 *
 * @param room    The unique numeric ID of the room.
 * @param streams A list of streams this subscription is attached to.
 */
public record AttachedEvent(long room, List<SubscriberStream> streams) {

    /**
     * Creates an {@link AttachedEvent} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link AttachedEvent} instance.
     * @throws IllegalArgumentException if the JSON does not represent a valid 'attached' event.
     */
    public static AttachedEvent fromJson(JSONObject json) {
        if (json == null || !"attached".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for AttachedEvent: " + json);
        }
        var streamsArray = json.getJSONArray("streams");
        List<SubscriberStream> streamList = IntStream.range(0, streamsArray.length())
            .mapToObj(streamsArray::getJSONObject)
            .map(SubscriberStream::fromJson)
            .collect(Collectors.toList());

        return new AttachedEvent(
            json.getLong("room"),
            streamList
        );
    }
}
