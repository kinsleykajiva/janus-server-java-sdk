package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the successful response from a 'list' rooms request.
 *
 * @param list A list of available video rooms.
 */
public record ListRoomsResponse(List<VideoRoom> list) {

    /**
     * Creates a {@link ListRoomsResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link ListRoomsResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful 'list' response.
     */
    public static ListRoomsResponse fromJson(JSONObject json) {
        if (json == null || !"success".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for ListRoomsResponse: " + json);
        }
        var jsonArray = json.getJSONArray("list");
        List<VideoRoom> rooms = IntStream.range(0, jsonArray.length())
            .mapToObj(jsonArray::getJSONObject)
            .map(VideoRoom::fromJson)
            .collect(Collectors.toList());
        return new ListRoomsResponse(rooms);
    }
}
