package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A request to configure an active subscriber's session (e.g., change stream properties or trigger an ICE restart).
 */
public class ConfigureSubscriberRequest {
    private final JSONObject json;

    private ConfigureSubscriberRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "configure");
        if (builder.restart != null) json.put("restart", builder.restart);
        if (builder.streams != null) {
            json.put("streams", builder.streams.stream()
                .map(SubscriberStreamUpdate::toJson)
                .collect(Collectors.toList()));
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
     * A builder for creating {@link ConfigureSubscriberRequest} instances.
     */
    public static class Builder {
        private Boolean restart;
        private List<SubscriberStreamUpdate> streams;

        public Builder setRestart(boolean restart) {
            this.restart = restart;
            return this;
        }

        public Builder setStreams(List<SubscriberStreamUpdate> streams) {
            this.streams = streams;
            return this;
        }

        public ConfigureSubscriberRequest build() {
            return new ConfigureSubscriberRequest(this);
        }
    }
}
