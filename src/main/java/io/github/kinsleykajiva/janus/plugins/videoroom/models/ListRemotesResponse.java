package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the successful response from a 'list_remotes' request.
 *
 * @param room The room the publisher is in.
 * @param id   The ID of the publisher.
 * @param list A list of active remotizations for the publisher.
 */
public record ListRemotesResponse(long room, long id, List<RemoteRemotization> list) {

    /**
     * Creates a {@link ListRemotesResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link ListRemotesResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful response.
     */
    public static ListRemotesResponse fromJson(JSONObject json) {
        if (json == null || !"success".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for ListRemotesResponse: " + json);
        }
        var remotizationsArray = json.getJSONArray("list");
        List<RemoteRemotization> remotizationList = IntStream.range(0, remotizationsArray.length())
            .mapToObj(remotizationsArray::getJSONObject)
            .map(RemoteRemotization::fromJson)
            .collect(Collectors.toList());

        return new ListRemotesResponse(
            json.getLong("room"),
            json.getLong("id"),
            remotizationList
        );
    }
}
