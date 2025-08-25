package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;

/**
 * A request to manage the token-based ACL of a video room.
 */
public class AllowedRequest {
    private final JSONObject json;

    private AllowedRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "allowed");
        json.put("action", builder.action.toString().toLowerCase());
        json.put("room", builder.room);
        if (builder.secret != null) json.put("secret", builder.secret);
        if (builder.allowed != null) json.put("allowed", builder.allowed);
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * The action to perform on the ACL.
     */
    public enum Action { ENABLE, DISABLE, ADD, REMOVE }

    /**
     * A builder for creating {@link AllowedRequest} instances.
     */
    public static class Builder {
        private final long room;
        private final Action action;
        private String secret;
        private List<String> allowed;

        /**
         * Creates a new builder for an AllowedRequest.
         * @param room The unique numeric ID of the room to update.
         * @param action The action to perform.
         */
        public Builder(long room, Action action) {
            this.room = room;
            this.action = action;
        }

        public Builder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        /**
         * Sets the list of tokens. This is required for ADD and REMOVE actions.
         * @param allowed A list of string tokens.
         * @return this builder.
         */
        public Builder setAllowed(List<String> allowed) {
            this.allowed = allowed;
            return this;
        }

        public AllowedRequest build() {
            if ((action == Action.ADD || action == Action.REMOVE) && (allowed == null || allowed.isEmpty())) {
                throw new IllegalStateException("Allowed token list cannot be null or empty for ADD or REMOVE actions.");
            }
            return new AllowedRequest(this);
        }
    }
}
