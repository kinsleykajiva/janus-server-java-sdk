package io.github.kinsleykajiva.janus.client.plugins.videoroom.events;

import org.json.JSONObject;

/**
 * An event indicating that a publisher has started talking.
 *
 * @param room            The unique numeric ID of the room.
 * @param id              The unique numeric ID of the publisher.
 * @param audioLevelDBovAvg The average audio level in dBov.
 */
public record TalkingEvent(long room, long id, int audioLevelDBovAvg) {

    /**
     * Creates a {@link TalkingEvent} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus event.
     * @return A new {@link TalkingEvent} instance.
     * @throws IllegalArgumentException if the JSON does not represent a valid 'talking' event.
     */
    public static TalkingEvent fromJson(JSONObject json) {
        if (json == null || !"talking".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for TalkingEvent: " + json);
        }
        return new TalkingEvent(
            json.getLong("room"),
            json.getLong("id"),
            json.getInt("audio-level-dBov-avg")
        );
    }
}
