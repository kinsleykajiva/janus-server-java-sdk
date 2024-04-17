package io.github.kinsleykajiva.transcoding;



import io.github.kinsleykajiva.SdkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MediaFactory class provides methods for creating and processing media files.
 */
@ApiStatus.NonExtendable
public class MediaFactory {
	static        Logger            log            = Logger.getLogger(MediaFactory.class.getName());
	private       MediaOutputTarget mediaOutputTarget;
	private       String            roomId;
	private       String            recordingFolder;
	private       String            outputFolder;
	private final List<FileInfoMJR> fileInfoMJRs   = new ArrayList<>();
	private final Set<String>       filesToCleanup = new HashSet<>();
	private       PostProcessing    postProcessing;
	
	
	private MediaFactory() {
		if (!SdkUtils.isJanusInstalled()) {
			throw new RuntimeException("Janus is not installed");
		}
		if (!SdkUtils.isFFMPEGInstalled()) {
			throw new RuntimeException("FFMPEG is not installed");
		}
		
	}
	
	public MediaFactory( @NotNull MediaOutputTarget mediaOutputTarget, @NotNull String roomId,
	                     @NotNull String recordingFolder, @NotNull String outputFolder, @Nullable PostProcessing postProcessingCallback ) {
		this();
		this.mediaOutputTarget = mediaOutputTarget;
		this.roomId = roomId;
		this.recordingFolder = SdkUtils.cleanFilePath(recordingFolder);
		this.outputFolder = SdkUtils.cleanFilePath(outputFolder);
		this.postProcessing = postProcessingCallback;
		// test if the recording folder exists
		if (!SdkUtils.folderExists(recordingFolder)) {
			if (postProcessingCallback != null) {
				postProcessingCallback.onProcessingFailed(roomId, System.currentTimeMillis(), "Recording folder does not exist");
			} else {
				throw new RuntimeException("Recording folder does not exist");
			}
			
		}
		// test if the output folder exists
		if (!SdkUtils.folderExists(outputFolder)) {
			if (postProcessingCallback != null) {
				postProcessingCallback.onProcessingFailed(roomId, System.currentTimeMillis(), "Output folder does not exist");
			} else {
				throw new RuntimeException("Output folder does not exist");
			}
			
		}
		if (mediaOutputTarget == MediaOutputTarget.VIDEO_ROOM_PLUGIN) {
			processForVideoRoom();
			
		}
		
	}
	
	/**
	 * Processes the files for a video room.
	 */
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
				fullFilePath = SdkUtils.cleanFilePath(fullFilePath);
				fileInfoMJRs.add(getFileMJRInfo(fullFilePath));
			});
			
			fileInfoMJRs.removeIf(Objects::isNull);// stream to remove null values in fileInfoMJRs
			if (postProcessing != null) {
				postProcessing.onProcessingStarted(roomId, System.currentTimeMillis(), fileInfoMJRs, Thread.currentThread());
			}
			if (fileInfoMJRs.size() % 2 != 0) {
				throw new RuntimeException("The files do not match . For each video mjr file there is a audio mjr file and vice versa");
			}
			var                              participantStreamsList  = processPairForUser();
			List<ParticipantStreamMediaFile> participantsFullStreams = createParticipantStreamMediaFile(participantStreamsList);
			// now lets combine all participant streams into a single media file who where in the same room.
			createVideoRoomFinalVideo(participantsFullStreams);
		} catch (IOException e) {
			throw new RuntimeException("Error reading directory", e);
		}
		
	}
	
	/**
	 * Creates the final video for a video room.
	 *
	 * @param participantsFullStreams The list of participant stream files.
	 */
	private void createVideoRoomFinalVideo( List<ParticipantStreamMediaFile> participantsFullStreams ) {
		participantsFullStreams.forEach(file -> {
			filesToCleanup.add(file.file().getAbsolutePath());
		});
		if (mediaOutputTarget == MediaOutputTarget.VIDEO_ROOM_PLUGIN) {
			
			String output = SdkUtils.cleanFilePath(outputFolder + File.separator + "final-videoroom-" + roomId + ".webm");
			output = getIncrementedFileName(output);
			//for one participant's streams.
			if (participantsFullStreams.size() == 1) {
				
				// move file to output folder
				try {
					Files.move(participantsFullStreams.getFirst().file().toPath(), Paths.get(output), StandardCopyOption.REPLACE_EXISTING);
					if (postProcessing != null) {
						postProcessing.onProcessingEnded(roomId, System.currentTimeMillis(), List.of(new File(output)), Thread.currentThread());
					}
				} catch (IOException e) {
					log.log(java.util.logging.Level.SEVERE, "Error moving file to output folder" + e.getMessage(), e);
					if (postProcessing != null) {
						postProcessing.onProcessingFailed(roomId, System.currentTimeMillis(), "Error moving file to output folder " + e.getMessage());
					}
				}
			}
			// sort participantsFullStreams by startTime where the oldest one is first.
			participantsFullStreams.sort(Comparator.comparingLong(ParticipantStreamMediaFile::startTime));
			//for two participant's streams.
			if (participantsFullStreams.size() == 2) {
				
				var   firstObject  = participantsFullStreams.get(0);
				var   secondObject = participantsFullStreams.get(1);
				long  timeDiff     = secondObject.startTime() - firstObject.startTime();
				float diffMs1      = (float) timeDiff / 1000; // In milliseconds
				float diffS1       = diffMs1 / 1000; // In seconds
				
				try {
					SdkUtils.bashExecute("ffmpeg -i " + firstObject.file().getAbsolutePath() + " -i " + secondObject.file().getAbsolutePath() +
							" -filter_complex " +
							"\"[0]pad=2*iw:ih[l];[1]setpts=PTS-STARTPTS+" + diffS1 + "/TB[1v];[l][1v]overlay=x=W/2[v];[1]adelay=" + diffMs1 + "|" + diffMs1 + "[1a];[0][1a]amix=inputs=2[a]\" " +
							"-map \"[v]\" -map \"[a]\" " + output);
					postProcessing.onProcessingEnded(roomId, System.currentTimeMillis(), List.of(new File(output)), Thread.currentThread());
				} catch (IOException | InterruptedException e) {
					log.log(java.util.logging.Level.SEVERE, "Error creating final video file" + e.getMessage(), e);
					if (postProcessing != null) {
						postProcessing.onProcessingFailed(roomId, System.currentTimeMillis(), "Error creating final video file " + e.getMessage());
					}
				}
			}
			//for three participant's streams.
			
			if (participantsFullStreams.size() == 3) {
				var   firstObject  = participantsFullStreams.get(0);
				var   secondObject = participantsFullStreams.get(1);
				var   thirdObject  = participantsFullStreams.get(2);
				long  timeDiff1    = secondObject.startTime() - firstObject.startTime();
				long  timeDiff2    = thirdObject.startTime() - secondObject.startTime();
				float diffMs1      = (float) timeDiff1 / 1000; // In milliseconds
				float diffS1       = diffMs1 / 1000; // In seconds
				float diffMs2      = (float) timeDiff2 / 1000; // In milliseconds
				float diffS2       = diffMs2 / 1000; // In seconds
				try {
					SdkUtils.bashExecute("ffmpeg -i " + firstObject.file().getAbsolutePath() + " -i " + secondObject.file().getAbsolutePath() + " -i " + thirdObject.file().getAbsolutePath() +
							" -filter_complex " +
							"\"[0]pad=3*iw:ih[l];[1]setpts=PTS-STARTPTS+" + diffS1 + "/TB[1v];[2]setpts=PTS-STARTPTS+" + diffS2 + "/TB[2v];[l][1v]overlay=x=W/3[v1];[v1][2v]overlay=x=2*W/3[v2];[1]adelay=" + diffMs1 + "|" + diffMs1 + "[1a];[2]adelay=" + diffMs2 + "|" + diffMs2 + "[2a];[0][1a][2a]amix=inputs=3[a]\" " +
							"-map \"[v2]\" -map \"[a]\" " + output);
					
					postProcessing.onProcessingEnded(roomId, System.currentTimeMillis(), List.of(new File(output)), Thread.currentThread());
				} catch (IOException | InterruptedException e) {
					log.log(java.util.logging.Level.SEVERE, "Error creating final video file" + e.getMessage(), e);
					if (postProcessing != null) {
						postProcessing.onProcessingFailed(roomId, System.currentTimeMillis(), "Error creating final video file " + e.getMessage());
					}
				}
			}
			
			//for four participant's streams. make this into a 2x2 grid.
			
			if (participantsFullStreams.size() == 4) {
				var   firstObject  = participantsFullStreams.get(0);
				var   secondObject = participantsFullStreams.get(1);
				var   thirdObject  = participantsFullStreams.get(2);
				var   fourthObject = participantsFullStreams.get(3);
				long  timeDiff1    = secondObject.startTime() - firstObject.startTime();
				long  timeDiff2    = thirdObject.startTime() - secondObject.startTime();
				long  timeDiff3    = fourthObject.startTime() - thirdObject.startTime();
				float diffMs1      = (float) timeDiff1 / 1000; // In milliseconds
				float diffS1       = diffMs1 / 1000; // In seconds
				float diffMs2      = (float) timeDiff2 / 1000; // In milliseconds
				float diffS2       = diffMs2 / 1000; // In seconds
				float diffMs3      = (float) timeDiff3 / 1000; // In milliseconds
				float diffS3       = diffMs3 / 1000; // In seconds
				try {
					SdkUtils.bashExecute("ffmpeg -i " + firstObject.file().getAbsolutePath()
							+ " -i " + secondObject.file().getAbsolutePath()
							+ " -i " + thirdObject.file().getAbsolutePath()
							+ " -i " + fourthObject.file().getAbsolutePath() + " -filter_complex \"[0]pad=2*iw:2*ih[l];[1]setpts=PTS-STARTPTS+"
							+ diffS1 + "/TB[1v];[2]setpts=PTS-STARTPTS+" + diffS2 + "/TB[2v];[3]setpts=PTS-STARTPTS+"
							+ diffS3 + "/TB[3v];[l][1v]overlay=x=W/2:y=0[v1];[l][2v]overlay=x=0:y=H/2[v2];[l][3v]overlay=x=W/2:y=H/2[v3];[1]adelay="
							+ diffMs1 + "|" + diffMs1 + "[1a];[2]adelay=" + diffMs2 + "|" + diffMs2 + "[2a];[3]adelay=" + diffMs3 + "|" + diffMs3 + "[3a];[0][1a][2a][3a]amix=inputs=4[a]\" " +
							"-map \"[v3]\" -map \"[a]\" " + output);
					postProcessing.onProcessingEnded(roomId, System.currentTimeMillis(), List.of(new File(output)), Thread.currentThread());
				} catch (IOException | InterruptedException e) {
					log.log(java.util.logging.Level.SEVERE, "Error creating final video file" + e.getMessage(), e);
					if (postProcessing != null) {
						postProcessing.onProcessingFailed(roomId, System.currentTimeMillis(), "Error creating final video file " + e.getMessage());
					}
					
				}
			}
			
			
			//for five participant's streams.
			
			if (participantsFullStreams.size() == 5) {
				
				
				var   firstObject5  = participantsFullStreams.get(0);
				var   secondObject5 = participantsFullStreams.get(1);
				var   thirdObject5  = participantsFullStreams.get(2);
				var   fourthObject5 = participantsFullStreams.get(3);
				var   fifthObject   = participantsFullStreams.get(4);
				long  timeDiff1_    = secondObject5.startTime() - firstObject5.startTime();
				long  timeDiff2_    = thirdObject5.startTime() - secondObject5.startTime();
				long  timeDiff3_    = fourthObject5.startTime() - thirdObject5.startTime();
				long  timeDiff4     = fifthObject.startTime() - fourthObject5.startTime();
				float diffMs1_      = (float) timeDiff1_ / 1000; // In milliseconds
				float diffS1_       = diffMs1_ / 1000; // In seconds
				float diffMs2_      = (float) timeDiff2_ / 1000; // In milliseconds
				float diffS2_       = diffMs2_ / 1000; // In seconds
				float diffMs3_      = (float) timeDiff3_ / 1000; // In milliseconds
				float diffS3_       = diffMs3_ / 1000; // In seconds
				float diffMs4       = (float) timeDiff4 / 1000; // In milliseconds
				float diffS4        = diffMs4 / 1000; // In seconds
				try {
					SdkUtils.bashExecute("ffmpeg -i " + firstObject5.file().getAbsolutePath()
							+ " -i " + secondObject5.file().getAbsolutePath()
							+ " -i " + thirdObject5.file().getAbsolutePath()
							+ " -i " + fourthObject5.file().getAbsolutePath()
							+ " -i " + fifthObject.file().getAbsolutePath() + " -filter_complex \"[0]pad=3*iw:2*ih[l];[1]setpts=PTS-STARTPTS+"
							+ diffS1_ + "/TB[1v];[2]setpts=PTS-STARTPTS+" + diffS2_ + "/TB[2v];[3]setpts=PTS-STARTPTS+"
							+ diffS3_ + "/TB[3v];[4]setpts=PTS-STARTPTS+"
							+ diffS4 + "/TB[4v];[l][1v]overlay=x=W/3:y=0[v1];[l][2v]overlay=x=2*W/3:y=0[v2];[l][3v]overlay=x=0:y=H/2[v3];[l][4v]overlay=x=W/3:y=H/2[v4];[1]adelay="
							+ diffMs1_ + "|" + diffMs1_ + "[1a];[2]adelay=" + diffMs2_ + "|" + diffMs2_ + "[2a];[3]adelay=" + diffMs3_ + "|" + diffMs3_ + "[3a];[4]adelay=" + diffMs4 + "|" + diffMs4 + "[4a];[0][1a][2a][3a][4a]amix=inputs=5[a]\" " +
							"-map \"[v4]\" -map \"[a]\" " + output);
					postProcessing.onProcessingEnded(roomId, System.currentTimeMillis(), List.of(new File(output)), Thread.currentThread());
				} catch (IOException | InterruptedException e) {
					log.log(java.util.logging.Level.SEVERE, "Error creating final video file" + e.getMessage(), e);
					if (postProcessing != null) {
						postProcessing.onProcessingFailed(roomId, System.currentTimeMillis(), "Error creating final video file " + e.getMessage());
					}
				}
			}
			
			//for six participant's streams.make it into 3×2 Grid
			if (participantsFullStreams.size() == 6) {
				var   firstObject6  = participantsFullStreams.get(0);
				var   secondObject6 = participantsFullStreams.get(1);
				var   thirdObject6  = participantsFullStreams.get(2);
				var   fourthObject6 = participantsFullStreams.get(3);
				var   fifthObject6  = participantsFullStreams.get(4);
				var   sixthObject6  = participantsFullStreams.get(5);
				long  timeDiff1_    = secondObject6.startTime() - firstObject6.startTime();
				long  timeDiff2_    = thirdObject6.startTime() - secondObject6.startTime();
				long  timeDiff3_    = fourthObject6.startTime() - thirdObject6.startTime();
				long  timeDiff4_    = fifthObject6.startTime() - fourthObject6.startTime();
				long  timeDiff5     = sixthObject6.startTime() - fifthObject6.startTime();
				float diffMs1_      = (float) timeDiff1_ / 1000; // In milliseconds
				float diffS1_       = diffMs1_ / 1000; // In seconds
				float diffMs2_      = (float) timeDiff2_ / 1000; // In milliseconds
				float diffS2_       = diffMs2_ / 1000; // In seconds
				float diffMs3_      = (float) timeDiff3_ / 1000; // In milliseconds
				float diffS3_       = diffMs3_ / 1000; // In seconds
				float diffMs4_      = (float) timeDiff4_ / 1000; // In milliseconds
				float diffS4_       = diffMs4_ / 1000; // In seconds
				float diffMs5       = (float) timeDiff5 / 1000; // In milliseconds
				float diffS5        = diffMs5 / 1000; // In seconds
				try {
					SdkUtils.bashExecute("ffmpeg -i " + firstObject6.file().getAbsolutePath()
							+ " -i " + secondObject6.file().getAbsolutePath()
							+ " -i " + thirdObject6.file().getAbsolutePath()
							+ " -i " + fourthObject6.file().getAbsolutePath()
							+ " -i " + fifthObject6.file().getAbsolutePath()
							+ " -i " + sixthObject6.file().getAbsolutePath() + " -filter_complex \"[0]pad=3*iw:2*ih[l];[1]setpts=PTS-STARTPTS+"
							+ diffS1_ + "/TB[1v];[2]setpts=PTS-STARTPTS+" + diffS2_ + "/TB[2v];[3]setpts=PTS-STARTPTS+"
							+ diffS3_ + "/TB[3v];[4]setpts=PTS-STARTPTS+"
							+ diffS4_ + "/TB[4v];[5]setpts=PTS-STARTPTS+"
							+ diffS5 + "/TB[5v];[l][1v]overlay=x=W/3:y=0[v1];[l][2v]overlay=x=2*W/3:y=0[v2];[l][3v]overlay=x=0:y=H/2[v3];[l][4v]overlay=x=W/3:y=H/2[v4];[l][5v]overlay=x=2*W/3:y=H/2[v5];[1]adelay="
							+ diffMs1_ + "|" + diffMs1_ + "[1a];[2]adelay=" + diffMs2_ + "|" + diffMs2_ + "[2a];[3]adelay=" + diffMs3_ + "|" + diffMs3_ + "[3a];[4]adelay=" + diffMs4_ + "|" + diffMs4_ + "[4a];[5]adelay=" + diffMs5 + "|" + diffMs5 + "[5a];[0][1a][2a][3a][4a][5a]amix=inputs=6[a]\" " +
							"-map \"[v5]\" -map \"[a]\" " + output);
					postProcessing.onProcessingEnded(roomId, System.currentTimeMillis(), List.of(new File(output)), Thread.currentThread());
					
				} catch (IOException | InterruptedException e) {
					log.log(java.util.logging.Level.SEVERE, "Error creating final video file" + e.getMessage(), e);
					if (postProcessing != null) {
						postProcessing.onProcessingFailed(roomId, System.currentTimeMillis(), "Error creating final video file " + e.getMessage());
					}
				}
			}
			try {
				cleanUpFiles();
			} catch (IOException e) {
				log.log(java.util.logging.Level.SEVERE, "Error cleaning up files" + e.getMessage(), e);
			}
			
		}
	}
	
	void moveFiles( List<File> files, File destDir ) throws IOException {
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		
		for (File file : files) {
			Files.move(file.toPath(), new File(destDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	List<File> filterRoomFiles( File[] files, String roomId ) {
		List<File> roomFiles = new ArrayList<>();
		
		for (File file : files) {
			if (file.getName().startsWith("videoroom-" + roomId)) {
				roomFiles.add(file);
			}
		}
		
		return roomFiles;
	}
	
	void deleteFiles( List<File> files ) throws IOException {
		for (File file : files) {
			Files.delete(file.toPath());
		}
	}
	
	List<File> filterFiles( List<File> files, String ext ) {
		List<File> filteredFiles = new ArrayList<>();
		
		for (File file : files) {
			if (file.getName().endsWith(ext)) {
				filteredFiles.add(file);
			}
		}
		
		return filteredFiles;
	}
	
	private void cleanUpFiles() throws IOException {
		if (postProcessing != null) {
			postProcessing.onCleanUpStarted(roomId, System.currentTimeMillis(), filesToCleanup);
		}
		
		filesToCleanup.forEach(file -> {
			Path filePath = Paths.get(file);
			if (Files.exists(filePath)) {
				try {
					Files.delete(filePath);
				} catch (IOException e) {
					if (postProcessing != null) {
						postProcessing.onCleanUpFailed(roomId, System.currentTimeMillis(), "Error deleting file " + file);
					}
					log.log(java.util.logging.Level.SEVERE, "Error deleting file" + e.getMessage(), e);
				}
			}
		});
		if (postProcessing != null) {
			postProcessing.onCleanUpEnded(roomId, System.currentTimeMillis());
		}
	}
	
	/**
	 * Creates ParticipantStreamMediaFile objects .These are full media file for each participant (Video and Audio merged together for each participant)
	 *
	 * @param participantStreamsList The list of ParticipantStream objects.
	 * @return A list of ParticipantStreamMediaFile objects.
	 */
	/**/
	private List<ParticipantStreamMediaFile> createParticipantStreamMediaFile( List<ParticipantStream> participantStreamsList ) {
		List<ParticipantStreamMediaFile> streamMediaFiles = new ArrayList<>();
		participantStreamsList.forEach(participantStream -> {
			String audioInput  = participantStream.audio().file().getAbsolutePath();
			String audioOutput = participantStream.audio().file().getAbsolutePath().replace(".mjr", ".opus");
			filesToCleanup.add(audioOutput);
			try {
				SdkUtils.bashExecute("janus-pp-rec " + audioInput + " " + audioOutput);
			} catch (IOException | InterruptedException e) {
				audioOutput = "";
				log.log(java.util.logging.Level.SEVERE, "exec error: " + e.getMessage(), e);
				if (postProcessing != null) {
					postProcessing.onProcessingFailed(roomId, System.currentTimeMillis(), "exec error: audio file " + e.getMessage());
				}
			}
			String videoInput  = participantStream.video().file().getAbsolutePath();
			String videoOutput = participantStream.video().file().getAbsolutePath().replace(".mjr", ".webm");
			filesToCleanup.add(videoOutput);
			try {
				SdkUtils.bashExecute("janus-pp-rec " + videoInput + " " + videoOutput);
			} catch (IOException | InterruptedException e) {
				videoOutput = "";
				log.log(java.util.logging.Level.SEVERE, "exec error: " + e.getMessage(), e);
				if (postProcessing != null) {
					postProcessing.onProcessingFailed(roomId, System.currentTimeMillis(), "exec error: video file " + e.getMessage());
				}
			}
			// compare the start time of the video and audio files get the oldest one
			long officialStartTime = getOldestTimestamp(participantStream.audio().startTime(), participantStream.video().startTime());
			if (!videoOutput.isEmpty()) {
				try {
					
					String output = recordingFolder + "/videoroom-" + participantStream.video().videoRoom() + "-user-" + participantStream.video().userId() + "-" + officialStartTime + "-merged-output.webm";
					
					SdkUtils.bashExecute("ffmpeg -i " + audioOutput + " -i " + videoOutput + " -c:v copy -c:a opus -strict experimental " + output);
					filesToCleanup.add(output);
					var mediaFile = new ParticipantStreamMediaFile(participantStream.video().videoRoom(), participantStream.video().userId(), officialStartTime, new File(output));
					streamMediaFiles.add(mediaFile);
				} catch (IOException | InterruptedException e) {
					log.log(java.util.logging.Level.SEVERE, "Error creating media file" + e.getMessage(), e);
					if (postProcessing != null) {
						postProcessing.onProcessingFailed(roomId, System.currentTimeMillis(), "Error creating media file " + e.getMessage());
					}
				}
			}
		});
		return streamMediaFiles;
	}
	
	private long getOldestTimestamp( long time1, long time2 ) {
		return Math.min(time1, time2);
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
	
	private String getIncrementedFileName( String filePathString ) {
		Path   filePath    = Paths.get(filePathString);
		String base        = filePath.getFileName().toString().replaceFirst("[.][^.]+$", "");
		String ext         = filePath.getFileName().toString().substring(filePath.getFileName().toString().lastIndexOf(".") + 1);
		Path   dir         = filePath.getParent();
		int    index       = 1;
		Path   newFilePath = filePath;
		
		while (Files.exists(newFilePath)) {
			newFilePath = Paths.get(dir.toString(), base + "_" + index + "." + ext);
			index++;
		}
		
		if (!filePath.equals(newFilePath)) {
			try {
				Files.move(filePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				return newFilePath.toString();
			}
		}
		
		return newFilePath.toString();
	}
}
