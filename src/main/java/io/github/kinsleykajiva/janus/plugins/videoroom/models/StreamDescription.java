package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A description for a specific media stream being published.
 *
 * @param mid         The unique mid of the stream to describe.
 * @param description The text description to associate with the stream.
 */
public record StreamDescription(String mid, String description) {

    /**
     * Gets the JSON representation of the stream description.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return new JSONObject()
            .put("mid", mid)
            .put("description", description);
    }
}
