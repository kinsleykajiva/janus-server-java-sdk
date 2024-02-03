package africa.jopen.sdk.transcoding;

import java.io.File;

/**
 * ParticipantStreamMediaFile represents a media file associated with a participant in a video room.
 * It contains the roomId, userId, startTime, and file associated with the participant.
 */
public record ParticipantStreamMediaFile (String roomId, String userId,long startTime, File file){
}
