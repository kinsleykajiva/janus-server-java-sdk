package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;

/**
 * A response to an allowed request.
 *
 * @param room    The unique numeric ID of the room.
 * @param allowed The updated list of allowed tokens.
 */
public record AllowedResponse(long room, List<String> allowed) {
    public static AllowedResponse fromJson(JSONObject json) {
        final var allowedJson = json.getJSONArray("allowed");
        final var allowed = allowedJson.toList().stream().map(Object::toString).collect(Collectors.toList());
        return new AllowedResponse(json.getLong("room"), allowed);
    }
}
