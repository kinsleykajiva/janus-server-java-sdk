package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to list the announcements in a room.
 *
 * @param room   The unique numeric ID of the room.
 * @param secret The secret required to manage the room, if any.
 */
public record ListAnnouncementsRequest(long room, String secret) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "listannouncements")
            .put("room", room);
        if (secret != null) json.put("secret", secret);
        return json;
    }
}
