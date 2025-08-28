package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request for a publisher to join a video room.
 */
public class JoinRoomRequest {
    private final JSONObject json;

    private JoinRoomRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "join");
        json.put("ptype", "publisher");
        json.put("room", builder.room);
        if (builder.id != null) json.put("id", builder.id);
        if (builder.display != null) json.put("display", builder.display);
        if (builder.token != null) json.put("token", builder.token);
        if (builder.metadata != null) json.put("metadata", builder.metadata);
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link JoinRoomRequest} instances.
     */
    public static class Builder {
        private final long room;
        private Long id;
        private String display;
        private String token;
        private JSONObject metadata;

        /**
         * Creates a new builder for a JoinRoomRequest.
         * @param room The unique numeric ID of the room to join.
         */
        public Builder(long room) {
            this.room = room;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setDisplay(String display) {
            this.display = display;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setMetadata(JSONObject metadata) {
            this.metadata = metadata;
            return this;
        }

        public JoinRoomRequest build() {
            return new JoinRoomRequest(this);
        }
    }
}
