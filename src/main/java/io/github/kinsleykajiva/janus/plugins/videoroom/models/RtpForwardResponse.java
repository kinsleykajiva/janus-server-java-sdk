package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the successful response from an 'rtp_forward' request.
 *
 * @param room        The unique numeric ID of the room.
 * @param publisherId The unique numeric ID of the publisher being forwarded.
 * @param forwarders  A list of active forwarders that were created.
 */
public record RtpForwardResponse(long room, long publisherId, List<Forwarder> forwarders) {

    /**
     * Creates an {@link RtpForwardResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link RtpForwardResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful response.
     */
    public static RtpForwardResponse fromJson(JSONObject json) {
        if (json == null || !"rtp_forward".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for RtpForwardResponse: " + json);
        }
        var forwardersArray = json.getJSONArray("forwarders");
        List<Forwarder> forwarderList = IntStream.range(0, forwardersArray.length())
            .mapToObj(forwardersArray::getJSONObject)
            .map(Forwarder::fromJson)
            .collect(Collectors.toList());

        return new RtpForwardResponse(
            json.getLong("room"),
            json.getLong("publisher_id"),
            forwarderList
        );
    }
}
