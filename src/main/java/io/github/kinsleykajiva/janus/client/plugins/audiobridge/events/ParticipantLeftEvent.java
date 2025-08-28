package io.github.kinsleykajiva.janus.client.plugins.audiobridge.events;

import org.json.JSONObject;

/**
 * An event indicating that a participant has left the room.
 *
 * @param roomId        The ID of the room.
 * @param participantId The ID of the participant who left.
 */
public record ParticipantLeftEvent(long roomId, long participantId) {

    /**
     * Creates a {@link ParticipantLeftEvent} from a {@link JSONObject}.
     *
     * @param json The JSON object from Janus, which must contain a "leaving" field.
     * @return A new instance of {@link ParticipantLeftEvent}.
     */
    public static ParticipantLeftEvent fromJson(JSONObject json) {
        return new ParticipantLeftEvent(
            json.getLong("room"),
            json.getLong("leaving")
        );
    }
}
