package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * A request to edit an existing video room. This class uses a builder pattern for convenience.
 */
public class EditRoomRequest {
    private final JSONObject json;

    private EditRoomRequest(Builder builder) {
        this.json = new JSONObject();
        json.put("request", "edit");
        json.put("room", builder.room);
        if (builder.secret != null) json.put("secret", builder.secret);
        if (builder.newDescription != null) json.put("new_description", builder.newDescription);
        if (builder.newSecret != null) json.put("new_secret", builder.newSecret);
        if (builder.newPin != null) json.put("new_pin", builder.newPin);
        if (builder.newIsPrivate != null) json.put("new_is_private", builder.newIsPrivate);
        if (builder.newRequirePvtid != null) json.put("new_require_pvtid", builder.newRequirePvtid);
        if (builder.newBitrate != null) json.put("new_bitrate", builder.newBitrate);
        if (builder.newFirFreq != null) json.put("new_fir_freq", builder.newFirFreq);
        if (builder.newPublishers != null) json.put("new_publishers", builder.newPublishers);
        if (builder.newLockRecord != null) json.put("new_lock_record", builder.newLockRecord);
        if (builder.permanent != null) json.put("permanent", builder.permanent);
    }

    /**
     * Gets the JSON representation of the request.
     * @return a {@link JSONObject}
     */
    public JSONObject toJson() {
        return json;
    }

    /**
     * A builder for creating {@link EditRoomRequest} instances.
     */
    public static class Builder {
        private final long room;
        private String secret;
        private String newDescription;
        private String newSecret;
        private String newPin;
        private Boolean newIsPrivate;
        private Boolean newRequirePvtid;
        private Integer newBitrate;
        private Integer newFirFreq;
        private Integer newPublishers;
        private Boolean newLockRecord;
        private Boolean permanent;

        /**
         * Creates a new builder for an EditRoomRequest.
         * @param room The unique numeric ID of the room to edit. This is mandatory.
         */
        public Builder(long room) {
            this.room = room;
        }

        public Builder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder setNewDescription(String newDescription) {
            this.newDescription = newDescription;
            return this;
        }

        public Builder setNewSecret(String newSecret) {
            this.newSecret = newSecret;
            return this;
        }

        public Builder setNewPin(String newPin) {
            this.newPin = newPin;
            return this;
        }

        public Builder setNewIsPrivate(boolean newIsPrivate) {
            this.newIsPrivate = newIsPrivate;
            return this;
        }

        public Builder setNewRequirePvtid(boolean newRequirePvtid) {
            this.newRequirePvtid = newRequirePvtid;
            return this;
        }

        public Builder setNewBitrate(int newBitrate) {
            this.newBitrate = newBitrate;
            return this;
        }

        public Builder setNewFirFreq(int newFirFreq) {
            this.newFirFreq = newFirFreq;
            return this;
        }

        public Builder setNewPublishers(int newPublishers) {
            this.newPublishers = newPublishers;
            return this;
        }

        public Builder setNewLockRecord(boolean newLockRecord) {
            this.newLockRecord = newLockRecord;
            return this;
        }

        public Builder setPermanent(boolean permanent) {
            this.permanent = permanent;
            return this;
        }

        public EditRoomRequest build() {
            return new EditRoomRequest(this);
        }
    }
}
