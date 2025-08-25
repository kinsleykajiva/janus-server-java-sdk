package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;

/**
 * A request to subscribe to one or more streams from publishers in a room.
 * This corresponds to a 'join' request with ptype 'subscriber'.
 */
public class SubscribeRequest {
    private final JSONObject json;

    private SubscribeRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "join");
        json.put("ptype", "subscriber");
        json.put("room", builder.room);
        if (builder.useMsid != null) json.put("use_msid", builder.useMsid);
        if (builder.autoupdate != null) json.put("autoupdate", builder.autoupdate);
        if (builder.privateId != null) json.put("private_id", builder.privateId);
        if (builder.streams != null) {
            json.put("streams", builder.streams.stream()
                .map(Subscription::toJson)
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
     * A builder for creating {@link SubscribeRequest} instances.
     */
    public static class Builder {
        private final long room;
        private final List<Subscription> streams;
        private Boolean useMsid;
        private Boolean autoupdate;
        private Long privateId;

        /**
         * Creates a new builder for a SubscribeRequest.
         * @param room    The unique numeric ID of the room to subscribe in.
         * @param streams A list of streams to subscribe to.
         */
        public Builder(long room, List<Subscription> streams) {
            this.room = room;
            this.streams = streams;
        }

        public Builder setUseMsid(boolean useMsid) {
            this.useMsid = useMsid;
            return this;
        }

        public Builder setAutoupdate(boolean autoupdate) {
            this.autoupdate = autoupdate;
            return this;
        }

        public Builder setPrivateId(long privateId) {
            this.privateId = privateId;
            return this;
        }

        public SubscribeRequest build() {
            return new SubscribeRequest(this);
        }
    }
}
