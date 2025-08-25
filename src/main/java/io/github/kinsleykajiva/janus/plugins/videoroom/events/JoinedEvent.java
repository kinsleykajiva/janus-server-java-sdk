package io.github.kinsleykajiva.janus.plugins.videoroom.events;

import io.github.kinsleykajiva.janus.plugins.videoroom.models.Publisher;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONObject;

/**
 * An event indicating the local user has successfully joined a room.
 *
 * @param room        The unique numeric ID of the room.
 * @param description The description of the room.
 * @param id          The unique ID assigned to the local user in the room.
 * @param privateId   A private ID for the user, used for associating subscriber handles.
 * @param publishers  A list of currently active publishers in the room.
 * @param attendees   A list of other non-publishing attendees (if `notify_joining` is enabled).
 */
public record JoinedEvent(
    long room,
    String description,
    long id,
    long privateId,
    List<Publisher> publishers,
    List<Attendee> attendees) {

    /**
     * Creates a {@link JoinedEvent} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link JoinedEvent} instance.
     * @throws IllegalArgumentException if the JSON does not represent a valid 'joined' event.
     */
    public static JoinedEvent fromJson(JSONObject json) {
        if (json == null || !"joined".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for JoinedEvent: " + json);
        }
        var publishersArray = json.getJSONArray("publishers");
        List<Publisher> publishers = IntStream.range(0, publishersArray.length())
            .mapToObj(publishersArray::getJSONObject)
            .map(Publisher::fromJson)
            .collect(Collectors.toList());

        List<Attendee> attendees = Collections.emptyList();
        if (json.has("attendees")) {
            var attendeesArray = json.getJSONArray("attendees");
            attendees = IntStream.range(0, attendeesArray.length())
                .mapToObj(attendeesArray::getJSONObject)
                .map(Attendee::fromJson)
                .collect(Collectors.toList());
        }

        return new JoinedEvent(
            json.getLong("room"),
            json.getString("description"),
            json.getLong("id"),
            json.getLong("private_id"),
            publishers,
            attendees
        );
    }
}
