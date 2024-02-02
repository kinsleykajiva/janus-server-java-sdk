package africa.jopen.sdk.transcoding;


import africa.jopen.sdk.SdkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public class MediaFactory {
	
	private MediaOutputTarget mediaOutputTarget;
	private String            roomId;
	private String            recordingFolder;
	private String            outputFolder;
	
	
	private MediaFactory() {
		if (!SdkUtils.isJanusInstalled()) {
			throw new RuntimeException("Janus is not installed");
		}
	}
	
	public MediaFactory( @NotNull MediaOutputTarget mediaOutputTarget, @NotNull String roomId, @NotNull String recordingFolder, @NotNull String outputFolder ) {
		this();
		this.mediaOutputTarget = mediaOutputTarget;
		this.roomId = roomId;
		this.recordingFolder = recordingFolder;
		this.outputFolder = outputFolder;
		// test if the recording folder exists
		if (!SdkUtils.folderExists(recordingFolder)) {
			throw new RuntimeException("Recording folder does not exist");
		}
		// test if the output folder exists
		if (!SdkUtils.folderExists(outputFolder)) {
			throw new RuntimeException("Output folder does not exist");
		}
	}
}
