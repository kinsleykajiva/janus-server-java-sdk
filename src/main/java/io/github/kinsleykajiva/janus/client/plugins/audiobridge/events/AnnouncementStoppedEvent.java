package io.github.kinsleykajiva.janus.client.plugins.audiobridge.events;

import org.json.JSONObject;

/**
 * An event indicating that an announcement has stopped in a room.
 *
 * @param room   The unique numeric ID of the room.
 * @param fileId The unique ID of the announcement.
 */
public record AnnouncementStoppedEvent(long room, String fileId) {
    public static AnnouncementStoppedEvent fromJson(JSONObject json) {
        return new AnnouncementStoppedEvent(
            json.getLong("room"),
            json.getString("file_id")
        );
    }
}
