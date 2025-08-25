package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A request to update a subscription by subscribing to new streams and/or
 * unsubscribing from existing ones in a single operation.
 */
public class UpdateSubscriptionRequest {
    private final JSONObject json;

    private UpdateSubscriptionRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "update");
        if (builder.subscribe != null && !builder.subscribe.isEmpty()) {
            json.put("subscribe", builder.subscribe.stream().map(Subscription::toJson).collect(Collectors.toList()));
        }
        if (builder.unsubscribe != null && !builder.unsubscribe.isEmpty()) {
            json.put("unsubscribe", builder.unsubscribe.stream().map(Unsubscription::toJson).collect(Collectors.toList()));
        }
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link UpdateSubscriptionRequest} instances.
     */
    public static class Builder {
        private List<Subscription> subscribe;
        private List<Unsubscription> unsubscribe;

        public Builder subscribe(List<Subscription> subscribe) {
            this.subscribe = subscribe;
            return this;
        }

        public Builder unsubscribe(List<Unsubscription> unsubscribe) {
            this.unsubscribe = unsubscribe;
            return this;
        }

        /**
         * Builds the {@link UpdateSubscriptionRequest} object.
         * @return a new instance of {@link UpdateSubscriptionRequest}.
         * @throws IllegalStateException if both subscribe and unsubscribe lists are empty or null.
         */
        public UpdateSubscriptionRequest build() {
             if ((subscribe == null || subscribe.isEmpty()) && (unsubscribe == null || unsubscribe.isEmpty())) {
                throw new IllegalStateException("Both subscribe and unsubscribe lists cannot be null or empty for an update request.");
            }
            return new UpdateSubscriptionRequest(this);
        }
    }
}
