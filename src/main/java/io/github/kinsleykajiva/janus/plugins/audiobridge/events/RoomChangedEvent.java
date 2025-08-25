package io.github.kinsleykajiva.janus.plugins.audiobridge.events;

import io.github.kinsleykajiva.janus.plugins.audiobridge.models.AudioBridgeParticipant;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An event indicating that the local user has successfully changed rooms.
 *
 * @param room         The ID of the new room.
 * @param id           The ID assigned to the local user in the new room.
 * @param display      The display name of the local user in the new room.
 * @param participants A list of other participants already in the new room.
 */
public record RoomChangedEvent(long room, long id, String display, List<AudioBridgeParticipant> participants) {
    public static RoomChangedEvent fromJson(JSONObject json) {
        final var participantsJson = json.getJSONArray("participants");
        final var participants = IntStream.range(0, participantsJson.length())
            .mapToObj(participantsJson::getJSONObject)
            .map(AudioBridgeParticipant::fromJson)
            .collect(Collectors.toList());
        return new RoomChangedEvent(
            json.getLong("room"),
            json.getLong("id"),
            json.optString("display"),
            participants
        );
    }
}
