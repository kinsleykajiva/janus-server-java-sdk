package io.github.kinsleykajiva.janus.plugins.videoroom.events;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An event indicating a subscription has been updated, usually as a result of an
 * `update` request. This event may be accompanied by a JSEP offer if a renegotiation
 * is required.
 *
 * @param room    The unique numeric ID of the room.
 * @param streams The updated list of streams in the subscription.
 */
public record UpdatedEvent(long room, List<SubscriberStream> streams) {

    /**
     * Creates an {@link UpdatedEvent} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link UpdatedEvent} instance.
     * @throws IllegalArgumentException if the JSON does not represent a valid 'updated' event.
     */
    public static UpdatedEvent fromJson(JSONObject json) {
        if (json == null || !"updated".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for UpdatedEvent: " + json);
        }
        var streamsArray = json.getJSONArray("streams");
        List<SubscriberStream> streamList = IntStream.range(0, streamsArray.length())
            .mapToObj(streamsArray::getJSONObject)
            .map(SubscriberStream::fromJson)
            .collect(Collectors.toList());

        return new UpdatedEvent(
            json.getLong("room"),
            streamList
        );
    }
}
