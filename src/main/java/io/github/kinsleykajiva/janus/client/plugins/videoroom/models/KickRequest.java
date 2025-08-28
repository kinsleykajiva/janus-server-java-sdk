package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to kick a participant from a video room.
 */
public class KickRequest {
    private final JSONObject json;

    /**
     * Creates a new KickRequest.
     * @param room   The unique numeric ID of the room.
     * @param id     The unique numeric ID of the participant to kick.
     * @param secret The room secret, mandatory if configured for the room.
     */
    public KickRequest(long room, long id, String secret) {
        this.json = new JSONObject();
        json.put("request", "kick");
        json.put("room", room);
        json.put("id", id);
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
