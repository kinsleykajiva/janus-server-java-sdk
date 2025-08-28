package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A helper class to define a stream for a remote publisher. Used in the `add_remote_publisher` request.
 */
public class RemoteStream {
    private final JSONObject json;

    private RemoteStream(Builder builder) {
        this.json = new JSONObject();
        json.put("type", builder.type);
        json.put("mindex", builder.mindex);
        json.put("mid", builder.mid);
        if (builder.disabled != null) json.put("disabled", builder.disabled);
        if (builder.codec != null) json.put("codec", builder.codec);
        if (builder.description != null) json.put("description", builder.description);
        if (builder.stereo != null) json.put("stereo", builder.stereo);
        if (builder.fec != null) json.put("fec", builder.fec);
        if (builder.dtx != null) json.put("dtx", builder.dtx);
        if (builder.h264Profile != null) json.put("h264-profile", builder.h264Profile);
        if (builder.vp9Profile != null) json.put("vp9-profile", builder.vp9Profile);
        if (builder.simulcast != null) json.put("simulcast", builder.simulcast);
        if (builder.svc != null) json.put("svc", builder.svc);
        if (builder.audioLevelExtId != null) json.put("audiolevel_ext_id", builder.audioLevelExtId);
        if (builder.videoOrientExtId != null) json.put("videoorient_ext_id", builder.videoOrientExtId);
        if (builder.playoutDelayExtId != null) json.put("playoutdelay_ext_id", builder.playoutDelayExtId);
    }

    /**
     * Gets the JSON representation of the remote stream definition.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link RemoteStream} instances.
     */
    public static class Builder {
        private final String type;
        private final String mindex;
        private final String mid;
        private Boolean disabled;
        private String codec;
        private String description;
        private Boolean stereo;
        private Boolean fec;
        private Boolean dtx;
        private String h264Profile;
        private String vp9Profile;
        private Boolean simulcast;
        private Boolean svc;
        private Integer audioLevelExtId;
        private Integer videoOrientExtId;
        private Integer playoutDelayExtId;

        public Builder(String type, String mindex, String mid) {
            this.type = type;
            this.mindex = mindex;
            this.mid = mid;
        }

        public Builder setDisabled(boolean disabled) { this.disabled = disabled; return this; }
        public Builder setCodec(String codec) { this.codec = codec; return this; }
        public Builder setDescription(String description) { this.description = description; return this; }
        public Builder setStereo(boolean stereo) { this.stereo = stereo; return this; }
        public Builder setFec(boolean fec) { this.fec = fec; return this; }
        public Builder setDtx(boolean dtx) { this.dtx = dtx; return this; }
        public Builder setH264Profile(String h264Profile) { this.h264Profile = h264Profile; return this; }
        public Builder setVp9Profile(String vp9Profile) { this.vp9Profile = vp9Profile; return this; }
        public Builder setSimulcast(boolean simulcast) { this.simulcast = simulcast; return this; }
        public Builder setSvc(boolean svc) { this.svc = svc; return this; }
        public Builder setAudioLevelExtId(int audioLevelExtId) { this.audioLevelExtId = audioLevelExtId; return this; }
        public Builder setVideoOrientExtId(int videoOrientExtId) { this.videoOrientExtId = videoOrientExtId; return this; }
        public Builder setPlayoutDelayExtId(int playoutDelayExtId) { this.playoutDelayExtId = playoutDelayExtId; return this; }

        public RemoteStream build() {
            return new RemoteStream(this);
        }
    }
}
