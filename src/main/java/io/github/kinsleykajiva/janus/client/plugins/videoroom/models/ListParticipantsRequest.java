package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to get a list of all participants in a specific video room.
 */
public class ListParticipantsRequest {
    private final JSONObject json;

    /**
     * Creates a new ListParticipantsRequest.
     * @param room The unique numeric ID of the room.
     */
    public ListParticipantsRequest(long room) {
        this.json = new JSONObject();
        json.put("request", "listparticipants");
        json.put("room", room);
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
