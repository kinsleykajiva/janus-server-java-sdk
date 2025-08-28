package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to change the room of the current participant.
 *
 * @param room            The unique numeric ID of the new room.
 * @param id              The new ID to assign to the participant.
 * @param pin             The PIN required to join the new room, if any.
 * @param group           The group to assign to the participant in the new room.
 * @param display         The new display name for the participant.
 * @param token           The token required to join the new room, if any.
 * @param muted           Whether the participant should be muted in the new room.
 * @param suspended       Whether the participant should be suspended in the new room.
 * @param pauseEvents     Whether to pause events for the participant in the new room.
 * @param bitrate         The bitrate to use for the participant in the new room.
 * @param quality         The quality to use for the participant in the new room.
 * @param expectedLoss    The expected loss to use for the participant in the new room.
 * @param volume          The volume to use for the participant in the new room.
 * @param spatialPosition The spatial position to use for the participant in the new room.
 * @param denoise         Whether to enable denoising for the participant in the new room.
 */
public record ChangeRoomRequest(
    long room,
    Long id,
    String pin,
    String group,
    String display,
    String token,
    Boolean muted,
    Boolean suspended,
    Boolean pauseEvents,
    Integer bitrate,
    Integer quality,
    Integer expectedLoss,
    Integer volume,
    Integer spatialPosition,
    Boolean denoise
) {
    public JSONObject toJson() {
        final var json = new JSONObject()
            .put("request", "changeroom")
            .put("room", room);
        if (id != null) json.put("id", id);
        if (pin != null) json.put("pin", pin);
        if (group != null) json.put("group", group);
        if (display != null) json.put("display", display);
        if (token != null) json.put("token", token);
        if (muted != null) json.put("muted", muted);
        if (suspended != null) json.put("suspended", suspended);
        if (pauseEvents != null) json.put("pause_events", pauseEvents);
        if (bitrate != null) json.put("bitrate", bitrate);
        if (quality != null) json.put("quality", quality);
        if (expectedLoss != null) json.put("expected_loss", expectedLoss);
        if (volume != null) json.put("volume", volume);
        if (spatialPosition != null) json.put("spatial_position", spatialPosition);
        if (denoise != null) json.put("denoise", denoise);
        return json;
    }
}
