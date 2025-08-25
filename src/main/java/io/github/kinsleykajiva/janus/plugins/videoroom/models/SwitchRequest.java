package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A request to "switch" a subscriber's media stream to a different publisher
 * without a full renegotiation.
 */
public class SwitchRequest {
    private final JSONObject json;

    /**
     * Creates a new SwitchRequest.
     * @param streams A list of {@link SwitchStream} objects defining the switch operations.
     */
    public SwitchRequest(List<SwitchStream> streams) {
        if (streams == null || streams.isEmpty()) {
            throw new IllegalArgumentException("Streams list cannot be null or empty for a switch request.");
        }
        this.json = new JSONObject();
        json.put("request", "switch");
        json.put("streams", streams.stream().map(SwitchStream::toJson).collect(Collectors.toList()));
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }
}
