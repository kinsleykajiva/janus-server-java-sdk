package africa.jopen.sdk.transcoding;

import java.io.File;

public record ParticipantStreamMediaFile (String roomId, String userId,long startTime, File file){
}
