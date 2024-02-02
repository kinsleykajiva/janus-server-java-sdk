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
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.NonExtendable
public class MediaFactory {
	static  Logger            log = Logger.getLogger(MediaFactory.class.getName());
	private MediaOutputTarget mediaOutputTarget;
	private String            roomId;
	private String            recordingFolder;
	private       String            outputFolder;
	private final List<FileInfoMJR> fileInfoMJRs = new ArrayList<>();
	
	
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
			processForVideoRoom();
			
		}
		
	}
	
	private void processForVideoRoom() {
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
			var participantStreamsList = processPairForUser();
			createParticipantStreamMediaFile(participantStreamsList);
			
			
		} catch (IOException e) {
			throw new RuntimeException("Error reading directory", e);
		}
		
	}
	
	private void createParticipantStreamMediaFile( List<ParticipantStream> participantStreamsList ){
		List<Map<String,String>> mediaFilesList = new ArrayList<>();
		participantStreamsList.forEach(participantStream -> {
			Map<String,String> mediaFiles = new HashMap<>();
			String audioInput = participantStream.audio() .file().getAbsolutePath();
			String audioOutput = participantStream.audio() .file().getAbsolutePath().replace(".mjr", ".opus");
			
			try {
				SdkUtils.bashExecute("janus-pp-rec " + audioInput+ " " +  audioOutput);
			} catch (IOException | InterruptedException e) {
				audioOutput= "";
				log.log(java.util.logging.Level.SEVERE, "exec error: " + e.getMessage(), e);
				
			}
			String videoInput = participantStream.video().file().getAbsolutePath();
			String videoOutput = participantStream.video().file().getAbsolutePath().replace(".mjr", ".webm");
			try {
				SdkUtils.bashExecute("janus-pp-rec " + videoInput+ " " +  videoOutput);
			} catch (IOException | InterruptedException e) {
				videoOutput= "";
				log.log(java.util.logging.Level.SEVERE, "exec error: " + e.getMessage(), e);
			}
			mediaFiles.put("audio",audioOutput);
			mediaFiles.put("video",videoOutput);
			mediaFilesList.add(mediaFiles);
		});
	}
	
	private List<ParticipantStream> processPairForUser() {
		Set<String> userIds = new HashSet<>();
		for (FileInfoMJR fileInfoMJR : fileInfoMJRs) {
			userIds.add(fileInfoMJR.userId());
		}
		List<ParticipantStream> participantStreams = new ArrayList<>();
		userIds.forEach(user -> {
			var pair = fileInfoMJRs.stream().filter(fileInfoMJR -> fileInfoMJR.userId().equals(user)).toArray(FileInfoMJR[]::new);
			// test if is audio or video
			if (pair[0].fileTypeVideoAudio().equals("audio")) {
				participantStreams.add(new ParticipantStream(user, pair[1], pair[0]));
			} else {
				participantStreams.add(new ParticipantStream(user, pair[0], pair[1]));
			}
		});
		
		return participantStreams;
		
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
