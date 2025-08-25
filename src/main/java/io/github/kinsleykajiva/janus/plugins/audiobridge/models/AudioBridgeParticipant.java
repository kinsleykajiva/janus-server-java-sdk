package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * Represents a participant in an AudioBridge room, detailing their state and properties.
 * This record is immutable and thread-safe.
 *
 * @param id              The unique numeric ID of the participant.
 * @param display         The display name of the participant, if provided.
 * @param setup           Indicates whether the user has successfully negotiated a WebRTC PeerConnection.
 * @param muted           Indicates if the user is currently muted.
 * @param suspended       Indicates if the user is currently suspended.
 * @param talking         Indicates if the user is currently talking (only available if audio levels are enabled).
 * @param spatialPosition The spatial panning of the participant's audio (0=left, 50=center, 100=right),
 *                        if spatial audio is enabled in the room.
 */
public record AudioBridgeParticipant(
    long id,
    String display,
    boolean setup,
    boolean muted,
    boolean suspended,
    boolean talking,
    int spatialPosition) {

    /**
     * Constructs an {@link AudioBridgeParticipant} from a {@link JSONObject}.
     * This factory method simplifies the creation of participant objects from JSON responses.
     *
     * @param json The {@link JSONObject} containing the participant's details.
     * @return A new instance of {@link AudioBridgeParticipant}.
     */
    public static AudioBridgeParticipant fromJson(JSONObject json) {
        return new AudioBridgeParticipant(
            json.getLong("id"),
            json.optString("display"),
            json.getBoolean("setup"),
            json.getBoolean("muted"),
            json.optBoolean("suspended"),
            json.optBoolean("talking"),
            json.optInt("spatial_position")
        );
    }
}
