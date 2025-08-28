package io.github.kinsleykajiva.janus.client.plugins.audiobridge.models;

import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A response to a stop_all_files request.
 *
 * @param room       The unique numeric ID of the room.
 * @param fileIdList The list of file IDs that were stopped.
 */
public record StopAllFilesResponse(long room, List<String> fileIdList) {
    public static StopAllFilesResponse fromJson(JSONObject json) {
        final var fileIdListJson = json.getJSONArray("file_id_list");
        final var fileIdList = fileIdListJson.toList().stream().map(Object::toString).collect(Collectors.toList());
        return new StopAllFilesResponse(json.getLong("room"), fileIdList);
    }
}
