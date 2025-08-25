package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to disable a specific remotization of a local publisher.
 */
public class UnpublishRemotelyRequest {
    private final JSONObject json;

    /**
     * Creates a new UnpublishRemotelyRequest.
     * @param room        The room the local publisher is in.
     * @param publisherId The ID of the local publisher.
     * @param remoteId    The ID of the remotization to disable.
     * @param secret      The room secret, mandatory if configured.
     */
    public UnpublishRemotelyRequest(long room, long publisherId, String remoteId, String secret) {
        this.json = new JSONObject();
        json.put("request", "unpublish_remotely");
        json.put("room", room);
        json.put("publisher_id", publisherId);
        json.put("remote_id", remoteId);
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
