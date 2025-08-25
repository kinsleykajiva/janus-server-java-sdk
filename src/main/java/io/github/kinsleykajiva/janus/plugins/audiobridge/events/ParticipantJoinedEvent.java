package io.github.kinsleykajiva.janus.plugins.audiobridge.events;

import io.github.kinsleykajiva.janus.plugins.audiobridge.models.AudioBridgeParticipant;
import org.json.JSONObject;

/**
 * An event indicating that a new participant has joined the room.
 *
 * @param roomId      The ID of the room.
 * @param participant The participant who joined.
 */
public record ParticipantJoinedEvent(long roomId, AudioBridgeParticipant participant) {

    /**
     * Creates a {@link ParticipantJoinedEvent} from a {@link JSONObject}.
     * This event is fired when the 'participants' array contains a new user.
     *
     * @param json The JSON object from Janus.
     * @return A new instance of {@link ParticipantJoinedEvent}, or null if the event is not a participant join event.
     */
    public static ParticipantJoinedEvent fromJson(JSONObject json) {
        if (!json.has("participants") || json.getJSONArray("participants").isEmpty()) {
            return null;
        }
        // Assuming the first participant in the list is the one who just joined.
        final var participantJson = json.getJSONArray("participants").getJSONObject(0);
        final var participant = AudioBridgeParticipant.fromJson(participantJson);
        return new ParticipantJoinedEvent(json.getLong("room"), participant);
    }
}
