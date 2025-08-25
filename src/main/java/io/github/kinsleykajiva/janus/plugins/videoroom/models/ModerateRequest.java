package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to moderate a participant's media stream (e.g., mute/unmute).
 */
public class ModerateRequest {
    private final JSONObject json;

    /**
     * Creates a new ModerateRequest.
     * @param room   The unique numeric ID of the room.
     * @param id     The unique numeric ID of the participant to moderate.
     * @param mid    The mid of the m-line to moderate.
     * @param mute   Whether to mute the media stream.
     * @param secret The room secret, mandatory if configured for the room.
     */
    public ModerateRequest(long room, long id, String mid, boolean mute, String secret) {
        this.json = new JSONObject();
        json.put("request", "moderate");
        json.put("room", room);
        json.put("id", id);
        json.put("mid", mid);
        json.put("mute", mute);
        if (secret != null) {
            json.put("secret", secret);
        }
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
