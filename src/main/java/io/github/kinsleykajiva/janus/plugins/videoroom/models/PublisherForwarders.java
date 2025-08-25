package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A helper class representing a publisher and their associated list of RTP forwarders.
 * This is part of the response to a `listforwarders` request.
 *
 * @param publisherId The unique numeric ID of the publisher.
 * @param forwarders  A list of {@link Forwarder}s associated with this publisher.
 */
public record PublisherForwarders(long publisherId, List<Forwarder> forwarders) {

    /**
     * Creates a {@link PublisherForwarders} instance from a {@link JSONObject}.
     *
     * @param json The JSON object representing the publisher's forwarders.
     * @return A new {@link PublisherForwarders} instance.
     */
    public static PublisherForwarders fromJson(JSONObject json) {
        var forwardersArray = json.getJSONArray("forwarders");
        List<Forwarder> forwarderList = IntStream.range(0, forwardersArray.length())
            .mapToObj(forwardersArray::getJSONObject)
            .map(Forwarder::fromJson)
            .collect(Collectors.toList());
        return new PublisherForwarders(json.getLong("publisher_id"), forwarderList);
    }
}
