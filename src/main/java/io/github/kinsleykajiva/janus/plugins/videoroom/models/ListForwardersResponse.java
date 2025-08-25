package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the successful response from a 'listforwarders' request.
 *
 * @param room       The unique numeric ID of the room.
 * @param publishers A list of publishers in the room that have active forwarders.
 */
public record ListForwardersResponse(long room, List<PublisherForwarders> publishers) {

    /**
     * Creates a {@link ListForwardersResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link ListForwardersResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful response.
     */
    public static ListForwardersResponse fromJson(JSONObject json) {
        if (json == null || !"forwarders".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for ListForwardersResponse: " + json);
        }
        var publishersArray = json.getJSONArray("publishers");
        List<PublisherForwarders> publisherList = IntStream.range(0, publishersArray.length())
            .mapToObj(publishersArray::getJSONObject)
            .map(PublisherForwarders::fromJson)
            .collect(Collectors.toList());
        return new ListForwardersResponse(json.getLong("room"), publisherList);
    }
}
