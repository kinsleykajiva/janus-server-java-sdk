package io.github.kinsleykajiva.transcoding;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * The PostProcessing interface defines the callback methods for handling the processing lifecycle of files.
 */
public interface PostProcessing {
	
	void onProcessingStarted( String roomId, long millisecondsTimeStamp, List<FileInfoMJR> fileInfoMJRs,Thread thread);
	
	void onProcessingEnded( String roomId, long millisecondsTimeStamp, List<File> outputs, Thread thread);
	
	void onProcessingFailed( String roomId, long millisecondsTimeStamp, String error);
	
	void onCleanUpStarted( String roomId, long millisecondsTimeStamp, Set<String> filesToCleanup);
	
	void onCleanUpFailed( String roomId, long millisecondsTimeStamp, String error);
	
	void onCleanUpEnded( String roomId, long millisecondsTimeStamp);
	
}
