package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to list all the active remotizations for a local publisher.
 */
public class ListRemotesRequest {
    private final JSONObject json;

    /**
     * Creates a new ListRemotesRequest.
     * @param room        The room the local publisher is in.
     * @param publisherId The ID of the local publisher.
     * @param secret      The room secret, mandatory if configured.
     */
    public ListRemotesRequest(long room, long publisherId, String secret) {
        this.json = new JSONObject();
        json.put("request", "list_remotes");
        json.put("room", room);
        json.put("publisher_id", publisherId);
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
