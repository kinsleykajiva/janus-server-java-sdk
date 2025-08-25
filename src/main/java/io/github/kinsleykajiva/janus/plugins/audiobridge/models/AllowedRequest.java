package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

import java.util.List;

/**
 * A request to manage the list of allowed users for a room.
 *
 * @param room    The unique numeric ID of the room.
 * @param secret  The secret required to manage the room, if any.
 * @param action  The action to perform (e.g., "add", "remove", "enable", "disable").
 * @param allowed A list of tokens to add or remove.
 */
public record AllowedRequest(long room, String secret, String action, List<String> allowed) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "allowed")
            .put("room", room)
            .put("action", action);
        if (secret != null) json.put("secret", secret);
        if (allowed != null) json.put("allowed", allowed);
        return json;
    }
}
