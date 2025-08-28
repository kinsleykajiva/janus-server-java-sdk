package io.github.kinsleykajiva.janus.client.plugins.audiobridge.events;

import org.json.JSONObject;

/**
 * An event indicating that an announcement has started in a room.
 *
 * @param room   The unique numeric ID of the room.
 * @param fileId The unique ID of the announcement.
 */
public record AnnouncementStartedEvent(long room, String fileId) {
    public static AnnouncementStartedEvent fromJson(JSONObject json) {
        return new AnnouncementStartedEvent(
            json.getLong("room"),
            json.getString("file_id")
        );
    }
}
