package io.github.kinsleykajiva.janus.client.plugins.videoroom.models;

import org.json.JSONObject;

/**
 * Represents a video room with its configuration properties.
 *
 * @param room                 The unique numeric ID of the room.
 * @param description          The pretty name of the room.
 * @param pinRequired          Whether a PIN is required to join this room.
 * @param isPrivate            Whether this room is 'private' (hidden from lists).
 * @param maxPublishers        The maximum number of concurrent publishers.
 * @param bitrate              The bitrate cap for publishers.
 * @param bitrateCap           Whether the bitrate cap is a hard limit.
 * @param firFreq              The frequency of FIR/PLI requests to publishers.
 * @param requirePvtid         Whether subscribers must provide a private_id.
 * @param requireE2ee          Whether end-to-end encryption is required.
 * @param dummyPublisher       Whether a dummy publisher exists for placeholder subscriptions.
 * @param notifyJoining        Whether to notify all participants when a new one joins.
 * @param audioCodec           The comma-separated list of allowed audio codecs.
 * @param videoCodec           The comma-separated list of allowed video codecs.
 * @param opusFec              Whether inband FEC is enabled for Opus.
 * @param opusDtx              Whether DTX is enabled for Opus.
 * @param record               Whether the room is being recorded.
 * @param recDir               The directory for recordings.
 * @param lockRecord           Whether recording can only be controlled via secret.
 * @param numParticipants      The current number of participants.
 * @param audioLevelExt        Whether the ssrc-audio-level RTP extension is used.
 * @param audioLevelEvent      Whether to emit audio level events.
 * @param audioActivePackets   The number of packets for audio level calculation.
 * @param audioLevelAverage    The average audio level threshold.
 * @param videoOrientExt       Whether the video-orientation RTP extension is used.
 * @param playoutDelayExt      Whether the playout-delay RTP extension is used.
 * @param transportWideCcExt   Whether the transport-wide-cc RTP extension is used.
 */
public record VideoRoom(
    long room,
    String description,
    boolean pinRequired,
    boolean isPrivate,
    int maxPublishers,
    int bitrate,
    boolean bitrateCap,
    int firFreq,
    boolean requirePvtid,
    boolean requireE2ee,
    boolean dummyPublisher,
    boolean notifyJoining,
    String audioCodec,
    String videoCodec,
    boolean opusFec,
    boolean opusDtx,
    boolean record,
    String recDir,
    boolean lockRecord,
    int numParticipants,
    boolean audioLevelExt,
    boolean audioLevelEvent,
    int audioActivePackets,
    int audioLevelAverage,
    boolean videoOrientExt,
    boolean playoutDelayExt,
    boolean transportWideCcExt) {

    /**
     * Creates a {@link VideoRoom} instance from a {@link JSONObject}.
     *
     * @param json The JSON object representing the video room.
     * @return A new {@link VideoRoom} instance, or null if the input is null.
     */
    public static VideoRoom fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        return new VideoRoom(
            json.getLong("room"),
            json.optString("description"),
            json.optBoolean("pin_required", false),
            json.optBoolean("is_private", false),
            json.getInt("max_publishers"),
            json.optInt("bitrate"),
            json.optBoolean("bitrate_cap", false),
            json.optInt("fir_freq"),
            json.optBoolean("require_pvtid", false),
            json.optBoolean("require_e2ee", false),
            json.optBoolean("dummy_publisher", false),
            json.optBoolean("notify_joining", false),
            json.optString("audiocodec"),
            json.optString("videocodec"),
            json.optBoolean("opus_fec", true),
            json.optBoolean("opus_dtx", false),
            json.optBoolean("record", false),
            json.optString("rec_dir"),
            json.optBoolean("lock_record", false),
            json.optInt("num_participants", -1), // Use a sentinel value if not present
            json.optBoolean("audiolevel_ext", true),
            json.optBoolean("audiolevel_event", false),
            json.optInt("audio_active_packets", 100),
            json.optInt("audio_level_average", 25),
            json.optBoolean("videoorient_ext", true),
            json.optBoolean("playoutdelay_ext", true),
            json.optBoolean("transport_wide_cc_ext", true)
        );
    }
}
