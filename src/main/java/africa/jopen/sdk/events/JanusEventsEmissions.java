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
	
	void onParticipantJoined( long participantId, String participantDisplay, String roomId );
	
	void onParticipantLeft( long participantId, String participantDisplay, String roomId );
	
	/**
	 * Event fired when the first participant joins the room
	 */
	void onRoomSessionStarted( String roomId, long firstParticipantId, String firstParticipantDisplay );
	
	/**
	 * Event fired when the last participant leaves the room, indicating an empty room
	 */
	void onRoomSessionEnded( String roomId );
	
	/**
	 * Event fired when a new event is received from Janus
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
