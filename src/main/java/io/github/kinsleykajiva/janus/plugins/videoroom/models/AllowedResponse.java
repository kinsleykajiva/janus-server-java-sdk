package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the successful response from an 'allowed' request.
 *
 * @param room    The unique numeric ID of the room.
 * @param allowed The updated, complete list of allowed tokens (if applicable).
 */
public record AllowedResponse(long room, List<String> allowed) {

    /**
     * Creates an {@link AllowedResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link AllowedResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful response.
     */
    public static AllowedResponse fromJson(JSONObject json) {
        if (json == null || !"success".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for AllowedResponse: " + json);
        }
        List<String> allowedList = Collections.emptyList();
        if (json.has("allowed")) {
            var allowedArray = json.getJSONArray("allowed");
            allowedList = IntStream.range(0, allowedArray.length())
                .mapToObj(allowedArray::getString)
                .collect(Collectors.toList());
        }
        return new AllowedResponse(json.getLong("room"), allowedList);
    }
}
