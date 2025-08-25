package io.github.kinsleykajiva.janus.plugins.videoroom.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONObject;

/**
 * Represents the successful response from a 'listparticipants' request.
 *
 * @param room         The unique numeric ID of the room.
 * @param participants A list of participants in the room.
 */
public record ListParticipantsResponse(long room, List<Participant> participants) {

    /**
     * Creates a {@link ListParticipantsResponse} instance from a {@link JSONObject}.
     *
     * @param json The JSON object from the Janus response.
     * @return A new {@link ListParticipantsResponse} instance.
     * @throws IllegalArgumentException if the JSON does not represent a successful 'participants' list.
     */
    public static ListParticipantsResponse fromJson(JSONObject json) {
        if (json == null || !"participants".equals(json.optString("videoroom"))) {
            throw new IllegalArgumentException("Invalid JSON for ListParticipantsResponse: " + json);
        }
        var jsonArray = json.getJSONArray("participants");
        List<Participant> participantList = IntStream.range(0, jsonArray.length())
            .mapToObj(jsonArray::getJSONObject)
            .map(Participant::fromJson)
            .collect(Collectors.toList());
        return new ListParticipantsResponse(json.getLong("room"), participantList);
    }
}
