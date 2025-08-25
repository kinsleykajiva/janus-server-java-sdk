package io.github.kinsleykajiva.janus.plugins.videoroom.events;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONObject;

/**
 * An event indicating a subscriber's stream has been successfully switched to a new source.
 *
 * @param room    The unique numeric ID of the room.
 * @param changes The number of successful stream changes.
 * @param streams The current configuration of all streams in the subscription.
 */
public record SwitchedEvent(long room, int changes, List<SubscriberStream> streams) {

    /**
     * Creates a {@link SwitchedEvent} instance from a {@link JSONObject}.
     * This event is identified by the presence of a 'switched' field with the value 'ok'.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link SwitchedEvent} instance.
     * @throws IllegalArgumentException if the JSON does not represent a valid 'switched' event.
     */
    public static SwitchedEvent fromJson(JSONObject json) {
        if (json == null || !"ok".equals(json.optString("switched"))) {
            throw new IllegalArgumentException("Invalid JSON for SwitchedEvent: " + json);
        }
        var streamsArray = json.getJSONArray("streams");
        List<SubscriberStream> streamList = IntStream.range(0, streamsArray.length())
            .mapToObj(streamsArray::getJSONObject)
            .map(SubscriberStream::fromJson)
            .collect(Collectors.toList());

        return new SwitchedEvent(
            json.getLong("room"),
            json.getInt("changes"),
            streamList
        );
    }
}
