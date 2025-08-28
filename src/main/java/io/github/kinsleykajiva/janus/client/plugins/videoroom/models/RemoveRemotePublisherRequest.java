package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to remove a previously created remote publisher.
 */
public class RemoveRemotePublisherRequest {
    private final JSONObject json;

    /**
     * Creates a new RemoveRemotePublisherRequest.
     * @param room   The room the remote publisher is in.
     * @param id     The ID of the remote publisher to remove.
     * @param secret The room secret, mandatory if configured.
     */
    public RemoveRemotePublisherRequest(long room, long id, String secret) {
        this.json = new JSONObject();
        json.put("request", "remove_remote_publisher");
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
