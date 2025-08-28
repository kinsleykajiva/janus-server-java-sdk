package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to edit an existing AudioBridge room.
 *
 * @param room           The unique numeric ID of the room to edit.
 * @param secret         The secret required to edit the room, if any.
 * @param newDescription A new description for the room.
 * @param newSecret      A new secret for the room.
 * @param newPin         A new PIN for the room.
 * @param newIsPrivate   Whether the room should be private.
 * @param newRecordDir   A new directory for recordings.
 * @param newMjrsDir     A new directory for MJR files.
 * @param permanent      Whether to make the changes permanent.
 */
public record EditRoomRequest(
    long room,
    String secret,
    String newDescription,
    String newSecret,
    String newPin,
    Boolean newIsPrivate,
    String newRecordDir,
    String newMjrsDir,
    Boolean permanent
) {
    public JSONObject toJson() {
        final var json = new JSONObject().put("request", "edit").put("room", room);
        if (secret != null) json.put("secret", secret);
        if (newDescription != null) json.put("new_description", newDescription);
        if (newSecret != null) json.put("new_secret", newSecret);
        if (newPin != null) json.put("new_pin", newPin);
        if (newIsPrivate != null) json.put("new_is_private", newIsPrivate);
        if (newRecordDir != null) json.put("new_record_dir", newRecordDir);
        if (newMjrsDir != null) json.put("new_mjrs_dir", newMjrsDir);
        if (permanent != null) json.put("permanent", permanent);
        return json;
    }
}
