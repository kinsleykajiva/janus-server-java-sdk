package io.github.kinsleykajiva.janus.plugins.audiobridge.models;

import java.util.List;
import org.json.JSONObject;

/**
 * Represents a request to create a new AudioBridge room. This class uses a builder pattern
 * to facilitate the construction of a request with many optional parameters.
 * The resulting object is immutable.
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
        if (builder.samplingRate != null) json.put("sampling_rate", builder.samplingRate);
        if (builder.spatialAudio != null) json.put("spatial_audio", builder.spatialAudio);
        if (builder.audioLevelExt != null) json.put("audiolevel_ext", builder.audioLevelExt);
        if (builder.audioLevelEvent != null) json.put("audiolevel_event", builder.audioLevelEvent);
        if (builder.audioActivePackets != null) json.put("audio_active_packets", builder.audioActivePackets);
        if (builder.audioLevelAverage != null) json.put("audio_level_average", builder.audioLevelAverage);
        if (builder.defaultExpectedLoss != null) json.put("default_expectedloss", builder.defaultExpectedLoss);
        if (builder.defaultBitrate != null) json.put("default_bitrate", builder.defaultBitrate);
        if (builder.denoise != null) json.put("denoise", builder.denoise);
        if (builder.record != null) json.put("record", builder.record);
        if (builder.recordFile != null) json.put("record_file", builder.recordFile);
        if (builder.recordDir != null) json.put("record_dir", builder.recordDir);
        if (builder.mjrs != null) json.put("mjrs", builder.mjrs);
        if (builder.mjrsDir != null) json.put("mjrs_dir", builder.mjrsDir);
        if (builder.allowRtpParticipants != null) json.put("allow_rtp_participants", builder.allowRtpParticipants);
        if (builder.groups != null) json.put("groups", builder.groups);
    }

    /**
     * Returns the JSON representation of this request object, to be sent to the Janus server.
     *
     * @return The {@link JSONObject}.
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
        private Integer samplingRate;
        private Boolean spatialAudio;
        private Boolean audioLevelExt;
        private Boolean audioLevelEvent;
        private Integer audioActivePackets;
        private Integer audioLevelAverage;
        private Integer defaultExpectedLoss;
        private Integer defaultBitrate;
        private Boolean denoise;
        private Boolean record;
        private String recordFile;
        private String recordDir;
        private Boolean mjrs;
        private String mjrsDir;
        private Boolean allowRtpParticipants;
        private List<String> groups;

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

        public Builder setSamplingRate(int samplingRate) {
            this.samplingRate = samplingRate;
            return this;
        }

        public Builder setSpatialAudio(boolean spatialAudio) {
            this.spatialAudio = spatialAudio;
            return this;
        }

        public Builder setAudioLevelExt(boolean audioLevelExt) {
            this.audioLevelExt = audioLevelExt;
            return this;
        }

        public Builder setAudioLevelEvent(boolean audioLevelEvent) {
            this.audioLevelEvent = audioLevelEvent;
            return this;
        }

        public Builder setAudioActivePackets(int audioActivePackets) {
            this.audioActivePackets = audioActivePackets;
            return this;
        }

        public Builder setAudioLevelAverage(int audioLevelAverage) {
            this.audioLevelAverage = audioLevelAverage;
            return this;
        }

        public Builder setDefaultExpectedLoss(int defaultExpectedLoss) {
            this.defaultExpectedLoss = defaultExpectedLoss;
            return this;
        }

        public Builder setDefaultBitrate(int defaultBitrate) {
            this.defaultBitrate = defaultBitrate;
            return this;
        }

        public Builder setDenoise(boolean denoise) {
            this.denoise = denoise;
            return this;
        }

        public Builder setRecord(boolean record) {
            this.record = record;
            return this;
        }

        public Builder setRecordFile(String recordFile) {
            this.recordFile = recordFile;
            return this;
        }

        public Builder setRecordDir(String recordDir) {
            this.recordDir = recordDir;
            return this;
        }

        public Builder setMjrs(boolean mjrs) {
            this.mjrs = mjrs;
            return this;
        }

        public Builder setMjrsDir(String mjrsDir) {
            this.mjrsDir = mjrsDir;
            return this;
        }

        public Builder setAllowRtpParticipants(boolean allowRtpParticipants) {
            this.allowRtpParticipants = allowRtpParticipants;
            return this;
        }

        public Builder setGroups(List<String> groups) {
            this.groups = groups;
            return this;
        }

        /**
         * Builds the {@link CreateRoomRequest}.
         *
         * @return A new instance of {@link CreateRoomRequest}.
         */
        public CreateRoomRequest build() {
            return new CreateRoomRequest(this);
        }
    }
}
