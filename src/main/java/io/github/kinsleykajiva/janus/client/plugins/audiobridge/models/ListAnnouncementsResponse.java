package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A response to a listannouncements request.
 *
 * @param room          The unique numeric ID of the room.
 * @param announcements The list of announcements in the room.
 */
public record ListAnnouncementsResponse(long room, List<Announcement> announcements) {
    public static ListAnnouncementsResponse fromJson(JSONObject json) {
        final var announcementsJson = json.getJSONArray("announcements");
        final var announcements = announcementsJson.toList().stream()
            .map(obj -> Announcement.fromJson(new JSONObject((java.util.Map) obj)))
            .collect(Collectors.toList());
        return new ListAnnouncementsResponse(json.getLong("room"), announcements);
    }
}
