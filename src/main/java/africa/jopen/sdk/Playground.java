package africa.jopen.sdk;

import africa.jopen.sdk.events.JanusEventsEmissions;
import africa.jopen.sdk.ffmpeg.RTPStreamEndpoint;
import africa.jopen.sdk.models.MySqlConfiguration;
import africa.jopen.sdk.mysql.DBAccess;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	
	// RTPStreamEndpoint
	public static void main( String[] args ) throws InterruptedException {
		RTPStreamEndpoint endpoint = new RTPStreamEndpoint(5014);
		Thread            thread   = new Thread(endpoint);
		thread.start();
		// Let the stream run for 10 seconds
		Thread.sleep(190_000);
		// ffmpeg -re -i MinaNawe.mp4 -vn -acodec opus -payload_type 111 -f rtp rtp://127.0.0.1:5044
		
//		// Let the stream run for a certain duration (e.g., 10 seconds)
//		Thread.sleep(10000);
//
//		// Stop the endpoint gracefully
//		endpoint.stop();
//		thread.join(); // Wait for the thread to finish execution
// Java equivalent code
		
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			System.out.println(e);
		}

// to Stop the endpoint gracefully
		endpoint.stop();
		
		try {
			thread.join(); // Wait for the thread to finish execution
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	public static void mainXX( String[] args ) {
		
		String             jsonContent = loadJsonFile("./samples/janus_log.json");
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
		
		
		
        /*JSONArray  jsonArray = SdkUtils.isJsonArray(event) ? new JSONArray(event) : new JSONArray().put(new JSONObject(event));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonEvent = jsonArray.getJSONObject(i);
            JanusEventsFactory janusEventsFactory = new JanusEventsFactory(jsonEvent, emissionsMock);
            janusEventsFactory.processEvent256();
        }*/
		//System.out.println(jsonContent);
		emissionsMock.consumeEventAsync(jsonContent);
		
		
	}
}
