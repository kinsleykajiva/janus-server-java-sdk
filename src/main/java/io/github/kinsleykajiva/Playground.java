package io.github.kinsleykajiva;



import io.github.kinsleykajiva.events.JanusEventsEmissions;
import io.github.kinsleykajiva.cache.mysql.MySqlConfiguration;
import io.github.kinsleykajiva.cache.mysql.DBAccess;
import io.github.kinsleykajiva.transcoding.FileInfoMJR;
import io.github.kinsleykajiva.transcoding.MediaFactory;
import io.github.kinsleykajiva.transcoding.MediaOutputTarget;
import io.github.kinsleykajiva.transcoding.PostProcessing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

//transcoding
public class Playground {
	public static String loadJsonFile(String filePath) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	public static void main( String[] args ) {
		
		MediaFactory mediaFactory = new MediaFactory(
				MediaOutputTarget.VIDEO_ROOM_PLUGIN ,
				"1234",
				"/var/www/janus/recording-folder",
				"/var/www/janus/recording-folder/processed",
				
				new PostProcessing(){
					@Override
					public void onProcessingStarted( String roomId, long millisecondsTimeStamp, List<FileInfoMJR> fileInfoMJRs, Thread thread ) {
						System.out.println("Processing started " + roomId + " " + millisecondsTimeStamp + " " + fileInfoMJRs + " " + thread);
					}
					
					@Override
					public void onProcessingEnded( String roomId, long millisecondsTimeStamp, List<File> outputs, Thread thread ) {
						System.out.println("Processing ended " + roomId + " " + millisecondsTimeStamp + " " + outputs + " " + thread);
					}
					
					@Override
					public void onProcessingFailed( String roomId, long millisecondsTimeStamp, String error ) {
						System.out.println("Processing failed " + roomId + " " + millisecondsTimeStamp + " " + error);
					}
					
					@Override
					public void onCleanUpStarted( String roomId, long millisecondsTimeStamp, Set<String> filesToCleanup ) {
						System.out.println("Clean up started " + roomId + " " + millisecondsTimeStamp + " " + filesToCleanup);
						
					}
					
					@Override
					public void onCleanUpFailed( String roomId, long millisecondsTimeStamp, String error ) {
						System.out.println("Clean up failed " + roomId + " " + millisecondsTimeStamp + " " + error);
					}
					
					@Override
					public void onCleanUpEnded( String roomId, long millisecondsTimeStamp ) {
						System.out.println("Clean up ended " + roomId + " " + millisecondsTimeStamp);
					}
				});
	}
	
	
	public static void main0( String[] args ) {
		
		String jsonContent = loadJsonFile("./samples/janus_log.json");
		//System.out.println("jsonContent = " + jsonContent);
		//System.exit(0);
		
		Janus.DB_ACCESS =new MySqlConfiguration("localhost", 3308, "janus_db","root","rootuser");
		DBAccess.getInstance(Janus.DB_ACCESS);
		JanusEventsEmissions emissionsMock = new JanusEventsEmissions() {
			@Override
			public void onParticipantJoined( long participantId, String participantDisplay, String roomId ) {
				System.out.println("Participant joined" + participantId + " " + participantDisplay + " " + roomId);
			}
			
			@Override
			public void onParticipantLeft( long participantId, String participantDisplay, String roomId ) {
				System.out.println("Participant left" + participantId + " " + participantDisplay + " " + roomId);
			}
			
			@Override
			public void onRoomSessionStarted( String roomId, long firstParticipantId, String firstParticipantDisplay ) {
				System.out.println("Room session started" + firstParticipantId + " " + firstParticipantDisplay);
			}
			
			@Override
			public void onRoomSessionEnded( String roomId ) {
				System.out.println("Room session ended");
			}
		};
		
		
		
        /*
        
        
        JSONArray  jsonArray = SdkUtils.isJsonArray(event) ? new JSONArray(event) : new JSONArray().put(new JSONObject(event));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonEvent = jsonArray.getJSONObject(i);
            JanusEventsFactory janusEventsFactory = new JanusEventsFactory(jsonEvent, emissionsMock);
            janusEventsFactory.processEvent256();
        }
        //-----
        
        */
		//System.out.println(jsonContent);
		emissionsMock.consumeEventAsync(jsonContent);
		
		
	}
}
