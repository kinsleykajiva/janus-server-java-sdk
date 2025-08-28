package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A response to a listforwarders request.
 *
 * @param room          The unique numeric ID of the room.
 * @param rtpForwarders The list of RTP forwarders in the room.
 */
public record ListForwardersResponse(long room, List<RtpForwarder> rtpForwarders) {
    public static ListForwardersResponse fromJson(JSONObject json) {
        final var forwardersJson = json.getJSONArray("rtp_forwarders");
        final var forwarders = forwardersJson.toList().stream()
            .map(obj -> RtpForwarder.fromJson(new JSONObject((java.util.Map) obj)))
            .collect(Collectors.toList());
        return new ListForwardersResponse(json.getLong("room"), forwarders);
    }
}
