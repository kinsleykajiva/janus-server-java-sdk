package io.github.kinsleykajiva.janus.plugins.audiobridge.events;

import io.github.kinsleykajiva.janus.plugins.audiobridge.models.AudioBridgeParticipant;
import org.json.JSONObject;

/**
 * An event indicating that a participant's state has been updated (e.g., muted, unmuted).
 *
 * @param roomId      The ID of the room.
 * @param participant The participant whose state was updated.
 */
public record ParticipantUpdatedEvent(long roomId, AudioBridgeParticipant participant) {

    /**
     * Creates a {@link ParticipantUpdatedEvent} from a {@link JSONObject}.
     * This is typically fired when the 'participants' array contains an updated user.
     *
     * @param json The JSON object from Janus.
     * @return A new instance of {@link ParticipantUpdatedEvent}, or null if it's not a participant update.
     */
    public static ParticipantUpdatedEvent fromJson(JSONObject json) {
         if (!json.has("participants") || json.getJSONArray("participants").isEmpty()) {
            return null;
        }
        // Assuming the first participant in the list is the one who was updated.
        final var participantJson = json.getJSONArray("participants").getJSONObject(0);
        final var participant = AudioBridgeParticipant.fromJson(participantJson);
        return new ParticipantUpdatedEvent(json.getLong("room"), participant);
    }
}
