package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents the successful response from a 'publish_remotely' request.
 *
 * @param room     The room the local publisher is in.
 * @param id       The ID of the local publisher.
 * @param remoteId The unique ID associated with this specific remotization.
 */
public record PublishRemotelyResponse(long room, long id, String remoteId) {

    /**
     * Creates a {@link PublishRemotelyResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link PublishRemotelyResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful response.
     */
    public static PublishRemotelyResponse fromJson(JSONObject json) {
        if (json == null || !"success".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for PublishRemotelyResponse: " + json);
        }
        return new PublishRemotelyResponse(
            json.getLong("room"),
            json.getLong("id"),
            json.getString("remote_id")
        );
    }
}
