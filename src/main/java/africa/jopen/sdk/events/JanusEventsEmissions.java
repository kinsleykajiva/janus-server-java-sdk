package africa.jopen.sdk.events;


import africa.jopen.sdk.SdkUtils;
import africa.jopen.sdk.models.events.EventType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public interface JanusEventsEmissions {
	
	ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	/**
	 * Callback method called when a participant joins a room.
	 *
	 * @param participantId       The ID of the participant who joined the room.
	 * @param participantDisplay  The display name of the participant who joined the room.
	 * @param roomId              The ID of the room which the participant joined.
	 */
	void onParticipantJoined( long participantId, String participantDisplay, String roomId );
	
	/**
	 * Callback method called when a participant leaves a room.
	 *
	 * @param participantId       The ID of the participant who left the room.
	 * @param participantDisplay  The display name of the participant who left the room.
	 * @param roomId              The ID of the room from which the participant left.
	 */
	void onParticipantLeft( long participantId, String participantDisplay, String roomId );
	
	/**
	 * Event fired when the first participant joins the room
	 * @param roomId the room id
	 * @param firstParticipantId the first participant id
	 * @param firstParticipantDisplay the first participant display name
	 */
	void onRoomSessionStarted( String roomId, long firstParticipantId, String firstParticipantDisplay );
	
	/**
	 * Callback method called when a room session ends.
	 *
	 * @param roomId The ID of the room that ended the session.
	 */
	void onRoomSessionEnded( String roomId );
	
	/**
	 * Event fired when a new event is received from Janus
	 * @param event the event json dump
	 */
	default void consumeEventAsync( String event ) {
		if(!SdkUtils.isJson(event)){
			return;
		}
		
		JSONArray jsonArray = SdkUtils.isJsonArray(event) ? new JSONArray(event) : new JSONArray().put(new JSONObject(event));
		IntStream.range(0, jsonArray.length())
				.parallel()
				.forEach(i -> executorService.submit(() -> {
					JSONObject jsonEvent = jsonArray.getJSONObject(i);
					JanusEventsFactory janusEventsFactory = new JanusEventsFactory(jsonEvent, this);
					EventType eventType = EventType.fromTypeValue(jsonEvent.getInt("type"));
					System.out.println(11  +" :: "+ eventType);
					switch (eventType) {
						case CORE -> janusEventsFactory.processEvent256();
						case TRANSPORT -> janusEventsFactory.processEvent128();
						case WEBRTC_STATE -> janusEventsFactory.processEvent16();
						case HANDLE -> janusEventsFactory.processEvent2();
						case MEDIA -> janusEventsFactory.processEvent32();
						case JSEP -> janusEventsFactory.processEvent8();
						case SESSION -> janusEventsFactory.processEvent1();
						case PLUGIN -> janusEventsFactory.processVideoRoomEvent(jsonEvent);
						default -> {
							// Handle the case where no other cases match, if necessary
						}
					}
				}));
	}
	
	
}
