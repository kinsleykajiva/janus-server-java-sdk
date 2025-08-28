package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * A request to reset the Opus decoder for the current participant.
 */
public record ResetDecoderRequest() {
    public JSONObject toJson() {
        return new JSONObject().put("request", "resetdecoder");
    }
}
