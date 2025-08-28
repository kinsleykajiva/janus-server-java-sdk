package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

import java.util.List;

/**
 * A request to create a new video room. This class uses a builder pattern to facilitate
 * the construction of a request with many optional parameters.
 */
public class CreateRoomRequest {
    private final JSONObject json;

    private CreateRoomRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "create");
        if (builder.room != null) json.put("room", builder.room);
        if (builder.permanent != null) json.put("permanent", builder.permanent);
        if (builder.description != null) json.put("description", builder.description);
        if (builder.secret != null) json.put("secret", builder.secret);
        if (builder.pin != null) json.put("pin", builder.pin);
        if (builder.isPrivate != null) json.put("is_private", builder.isPrivate);
        if (builder.allowed != null) json.put("allowed", builder.allowed);
        if (builder.publishers != null) json.put("publishers", builder.publishers);
        if (builder.bitrate != null) json.put("bitrate", builder.bitrate);
        if (builder.firFreq != null) json.put("fir_freq", builder.firFreq);
        if (builder.videocodec != null) json.put("videocodec", builder.videocodec);
        if (builder.record != null) json.put("record", builder.record);
        if (builder.notifyJoining != null) json.put("notify_joining", builder.notifyJoining);
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link CreateRoomRequest} instances.
     */
    public static class Builder {
        private Long room;
        private Boolean permanent;
        private String description;
        private String secret;
        private String pin;
        private Boolean isPrivate;
        private List<String> allowed;
        private Integer publishers;
        private Integer bitrate;
        private Integer firFreq;
        private String videocodec;
        private Boolean record;
        private Boolean notifyJoining;

        public Builder setRoom(long room) {
            this.room = room;
            return this;
        }

        public Builder setPermanent(boolean permanent) {
            this.permanent = permanent;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder setPin(String pin) {
            this.pin = pin;
            return this;
        }

        public Builder setIsPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
            return this;
        }

        public Builder setAllowed(List<String> allowed) {
            this.allowed = allowed;
            return this;
        }

        public Builder setPublishers(int publishers) {
            this.publishers = publishers;
            return this;
        }

        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        public Builder setFirFreq(int firFreq) {
            this.firFreq = firFreq;
            return this;
        }

        public Builder setVideocodec(String videocodec) {
            this.videocodec = videocodec;
            return this;
        }

        public Builder setRecord(boolean record) {
            this.record = record;
            return this;
        }

        public Builder setNotifyJoining(boolean notifyJoining) {
            this.notifyJoining = notifyJoining;
            return this;
        }

        public CreateRoomRequest build() {
            return new CreateRoomRequest(this);
        }
    }
}
