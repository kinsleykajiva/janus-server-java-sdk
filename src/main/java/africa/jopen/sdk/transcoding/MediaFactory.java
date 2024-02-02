package africa.jopen.sdk.transcoding;


import africa.jopen.sdk.SdkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.NonExtendable
public class MediaFactory {
	
	private MediaOutputTarget mediaOutputTarget;
	private String            roomId;
	private String            recordingFolder;
	private String            outputFolder;
	private List<FileInfoMJR> fileInfoMJRs = new ArrayList<>();
	
	
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
		if (mediaOutputTarget == MediaOutputTarget.VIDEO_ROOM_PLUGIN) {
			// look for files start with 'videoroom' ends with mjr in folder recordingFolder
			List<String> matchingFiles = new ArrayList<>();
			Path         dir           = Paths.get(recordingFolder);
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
				for (Path path : stream) {
					String fileName = path.getFileName().toString();
					if (fileName.startsWith("videoroom-" + roomId) && fileName.endsWith(".mjr")) {
						matchingFiles.add(fileName);
					}
				}
				matchingFiles.forEach(file -> {
					var  fullFilePath = file;
					Path filePath     = Paths.get(file);
					
					if (!Files.exists(filePath)) {
						// is not full path,its just name
						fullFilePath = recordingFolder + File.separator + file;
					}
					fileInfoMJRs.add(getFileMJRInfo(fullFilePath));
				});
				
				fileInfoMJRs.removeIf(Objects::isNull);// stream to remove null values in fileInfoMJRs
				
				if (fileInfoMJRs.size() % 2 != 0) {
					throw new RuntimeException("The files do not match . For each video mjr file there is a audio mjr file and vice versa");
				}
			} catch (IOException e) {
				throw new RuntimeException("Error reading directory", e);
			}
			
			
		}
		
	}
	
	public FileInfoMJR getFileMJRInfo( String filePath ) {
		if (filePath == null || filePath.isEmpty()) {
			return null;
		}
		
		if (mediaOutputTarget == MediaOutputTarget.VIDEO_ROOM_PLUGIN) {
			Pattern pattern = Pattern.compile("videoroom-(\\w+)-user-(\\d+)-(\\d+)-(audio|video)-(\\d+)\\.mjr");
			Matcher matcher = pattern.matcher(filePath);
			
			if (matcher.find()) {
				String videoRoom = matcher.group(1);
				String userId    = matcher.group(2);
				String startTime = matcher.group(3);
				String fileType  = matcher.group(4);
				String id        = matcher.group(5);
				
				return new FileInfoMJR(videoRoom, userId, Long.parseLong(startTime), fileType, id, new File(filePath));
			}
		}
		
		return null;
	}
}
