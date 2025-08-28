package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to globally enable or disable recording for all participants in a room.
 */
public class EnableRecordingRequest {
    private final JSONObject json;

    /**
     * Creates a new EnableRecordingRequest.
     * @param room   The unique numeric ID of the room.
     * @param record Whether to enable or disable recording.
     * @param secret The room secret, mandatory if `lock_record` is enabled for the room.
     */
    public EnableRecordingRequest(long room, boolean record, String secret) {
        this.json = new JSONObject();
        json.put("request", "enable_recording");
        json.put("room", room);
        json.put("record", record);
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
