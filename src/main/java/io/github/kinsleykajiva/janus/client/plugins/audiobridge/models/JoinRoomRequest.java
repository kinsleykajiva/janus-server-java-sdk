package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

/**
 * Represents a request for a participant to join an AudioBridge room.
 * This class uses a builder pattern for easy construction.
 */
public class JoinRoomRequest {
    private final JSONObject json;

    @SuppressWarnings("D")
    private JoinRoomRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "join");
        json.put("room", builder.room);
        if (builder.id != null) json.put("id", builder.id);
        if (builder.group != null) json.put("group", builder.group);
        if (builder.pin != null) json.put("pin", builder.pin);
        if (builder.display != null) json.put("display", builder.display);
        if (builder.token != null) json.put("token", builder.token);
        if (builder.muted != null) json.put("muted", builder.muted);
        if (builder.suspended != null) json.put("suspended", builder.suspended);
        if (builder.pauseEvents != null) json.put("pause_events", builder.pauseEvents);
        if (builder.codec != null) json.put("codec", builder.codec);
        if (builder.bitrate != null) json.put("bitrate", builder.bitrate);
        if (builder.quality != null) json.put("quality", builder.quality);
        if (builder.expectedLoss != null) json.put("expected_loss", builder.expectedLoss);
        if (builder.volume != null) json.put("volume", builder.volume);
        if (builder.spatialPosition != null) json.put("spatial_position", builder.spatialPosition);
        if (builder.denoise != null) json.put("denoise", builder.denoise);
        if (builder.secret != null) json.put("secret", builder.secret);
        if (builder.audioLevelAverage != null) json.put("audio_level_average", builder.audioLevelAverage);
        if (builder.audioActivePackets != null) json.put("audio_active_packets", builder.audioActivePackets);
        if (builder.record != null) json.put("record", builder.record);
        if (builder.filename != null) json.put("filename", builder.filename);
    }

    /**
     * Returns the JSON representation of this request object.
     * @return The {@link JSONObject}.
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
        private String group;
        private String pin;
        private String display;
        private String token;
        private Boolean muted;
        private Boolean suspended;
        private Boolean pauseEvents;
        private String codec;
        private Integer bitrate;
        private Integer quality;
        private Integer expectedLoss;
        private Integer volume;
        private Integer spatialPosition;
        private Boolean denoise;
        private String secret;
        private String audioLevelAverage;
        private String audioActivePackets;
        private Boolean record;
        private String filename;

        /**
         * The only mandatory parameter is the room ID.
         * @param room The numeric ID of the room to join.
         */
        public Builder(long room) {
            this.room = room;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setPin(String pin) {
            this.pin = pin;
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

        public Builder setMuted(boolean muted) {
            this.muted = muted;
            return this;
        }

        public Builder setSuspended(boolean suspended) {
            this.suspended = suspended;
            return this;
        }

        public Builder setPauseEvents(boolean pauseEvents) {
            this.pauseEvents = pauseEvents;
            return this;
        }

        public Builder setCodec(String codec) {
            this.codec = codec;
            return this;
        }

        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        public Builder setQuality(int quality) {
            this.quality = quality;
            return this;
        }

        public Builder setExpectedLoss(int expectedLoss) {
            this.expectedLoss = expectedLoss;
            return this;
        }

        public Builder setVolume(int volume) {
            this.volume = volume;
            return this;
        }

        public Builder setSpatialPosition(int spatialPosition) {
            this.spatialPosition = spatialPosition;
            return this;
        }

        public Builder setDenoise(boolean denoise) {
            this.denoise = denoise;
            return this;
        }

        public Builder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder setAudioLevelAverage(String audioLevelAverage) {
            this.audioLevelAverage = audioLevelAverage;
            return this;
        }

        public Builder setAudioActivePackets(String audioActivePackets) {
            this.audioActivePackets = audioActivePackets;
            return this;
        }

        public Builder setRecord(boolean record) {
            this.record = record;
            return this;
        }

        public Builder setFilename(String filename) {
            this.filename = filename;
            return this;
        }


        /**
         * Builds the {@link JoinRoomRequest}.
         * @return A new instance of {@link JoinRoomRequest}.
         */
        public JoinRoomRequest build() {
            return new JoinRoomRequest(this);
        }
    }
}
