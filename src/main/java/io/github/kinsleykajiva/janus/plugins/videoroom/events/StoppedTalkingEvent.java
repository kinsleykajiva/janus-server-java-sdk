package io.github.kinsleykajiva.janus.plugins.videoroom.events;

import org.json.JSONObject;

/**
 * An event indicating that a publisher has stopped talking.
 *
 * @param room            The unique numeric ID of the room.
 * @param id              The unique numeric ID of the publisher.
 * @param audioLevelDBovAvg The average audio level in dBov.
 */
public record StoppedTalkingEvent(long room, long id, int audioLevelDBovAvg) {

    /**
     * Creates a {@link StoppedTalkingEvent} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link StoppedTalkingEvent} instance.
     * @throws IllegalArgumentException if the JSON does not represent a valid 'stopped-talking' event.
     */
    public static StoppedTalkingEvent fromJson(JSONObject json) {
        if (json == null || !"stopped-talking".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for StoppedTalkingEvent: " + json);
        }
        return new StoppedTalkingEvent(
            json.getLong("room"),
            json.getLong("id"),
            json.getInt("audio-level-dBov-avg")
        );
    }
}
