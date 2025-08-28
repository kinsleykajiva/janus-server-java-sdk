package io.github.kinsleykajiva.janus.client.plugins.audiobridge.events;

import io.github.kinsleykajiva.janus.client.plugins.audiobridge.models.AudioBridgeParticipant;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An event indicating that the local user has successfully joined an AudioBridge room.
 *
 * @param roomId        The ID of the room that was joined.
 * @param participantId The ID assigned to the local user in this room.
 * @param display       The display name of the local user.
 * @param participants  A list of other participants already in the room.
 */
public record JoinedEvent(long roomId, long participantId, String display, List<AudioBridgeParticipant> participants) {

    /**
     * Creates a {@link JoinedEvent} from a {@link JSONObject}.
     *
     * @param json The JSON object from Janus.
     * @return A new instance of {@link JoinedEvent}.
     */
    public static JoinedEvent fromJson(JSONObject json) {
        final var participantsJson = json.getJSONArray("participants");
        final var participants = IntStream.range(0, participantsJson.length())
            .mapToObj(participantsJson::getJSONObject)
            .map(AudioBridgeParticipant::fromJson)
            .collect(Collectors.toList());

        return new JoinedEvent(
            json.getLong("room"),
            json.getLong("id"),
            json.optString("display"),
            participants
        );
    }
}
