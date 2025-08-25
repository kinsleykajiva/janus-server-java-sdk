package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a publisher in a video room, who is an active participant sending media.
 *
 * @param id        The unique numeric ID of the publisher.
 * @param display   The display name of the publisher.
 * @param metadata  A JSON object containing metadata about the publisher.
 * @param dummy     Whether this is a dummy publisher.
 * @param streams   A list of media streams this publisher is sending.
 * @param talking   Whether the publisher is currently talking (deprecated).
 */
public record Publisher(
    long id,
    String display,
    JSONObject metadata,
    boolean dummy,
    List<Stream> streams,
    boolean talking) {

    /**
     * Creates a {@link Publisher} instance from a {@link JSONObject}.
     *
     * @param json The JSON object representing the publisher.
     * @return A new {@link Publisher} instance, or null if the input is null.
     */
    public static Publisher fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        List<Stream> streams = Collections.emptyList();
        if (json.has("streams")) {
            streams = IntStream.range(0, json.getJSONArray("streams").length())
                .mapToObj(i -> Stream.fromJson(json.getJSONArray("streams").getJSONObject(i)))
                .collect(Collectors.toList());
        }

        return new Publisher(
            json.getLong("id"),
            json.optString("display"),
            json.optJSONObject("metadata"),
            json.optBoolean("dummy", false),
            streams,
            json.optBoolean("talking", false)
        );
    }
}
