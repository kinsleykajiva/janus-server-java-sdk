package africa.jopen.sdk.transcoding;

import java.io.File;
import java.util.List;

/**
 * The PostProcessing interface defines the callback methods for handling the processing lifecycle of files.
 */
public interface PostProcessing {
	
	void onProcessingStarted( String roomId, long millisecondsTimeStamp, List<FileInfoMJR> fileInfoMJRs,Thread thread);
	
	void onProcessingEnded( String roomId, long millisecondsTimeStamp, List<File> outputs, Thread thread);
	
	void onProcessingFailed( String roomId, long millisecondsTimeStamp, String error);
	
	
}
