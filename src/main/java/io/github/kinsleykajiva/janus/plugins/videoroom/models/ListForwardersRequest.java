package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to list all active RTP forwarders in a room.
 */
public class ListForwardersRequest {
    private final JSONObject json;

    /**
     * Creates a new ListForwardersRequest.
     * @param room   The room to get the list of forwarders from.
     * @param secret The room secret, mandatory if configured.
     */
    public ListForwardersRequest(long room, String secret) {
        this.json = new JSONObject();
        json.put("request", "listforwarders");
        json.put("room", room);
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
