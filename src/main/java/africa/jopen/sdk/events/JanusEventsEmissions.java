package africa.jopen.sdk.events;


import africa.jopen.sdk.Janus;
import africa.jopen.sdk.SdkUtils;
import africa.jopen.sdk.models.events.*;
import africa.jopen.sdk.mysql.DBAccess;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface JanusEventsEmissions {
	
	Map<String, List<ParticipantPojo>> videoRoomMap    = new ConcurrentHashMap<>();
	ExecutorService                    executorService = Executors.newSingleThreadExecutor();//consider to use virtual threads if your using jdk 21 and plus
	
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
		JSONArray jsonArray = SdkUtils.isJsonArray(event) ? new JSONArray(event) : new JSONArray().put(new JSONObject(event));
		for (int i = 0; i < jsonArray.length(); i++) {
			int finalI = i;
			executorService.submit(() -> {
				JanusEventsFactory janusEventsFactory = new JanusEventsFactory(jsonArray.getJSONObject(finalI), this);
				JSONObject         jsonEvent          = jsonArray.getJSONObject(finalI);
				EventType          eventType          = EventType.fromTypeValue(jsonEvent.getInt("type"));
				switch (eventType) {
					case CORE -> janusEventsFactory.processEvent256();
					case TRANSPORT -> janusEventsFactory.processEvent128();
					case WEBRTC_STATE -> janusEventsFactory.processEvent16();
					case HANDLE -> janusEventsFactory.processEvent2();
					case MEDIA -> janusEventsFactory.processEvent32();
					case JSEP -> janusEventsFactory.processEvent8();
					case SESSION -> janusEventsFactory.processEvent1();
					case PLUGIN -> {
						
						processVideoRoomEvent(jsonEvent);
						
					}
					default -> {
						// Handle the case where no other cases match, if necessary
					}
				}
			});
		}
	}
	
	private void processVideoRoomEvent( JSONObject jsonEvent ) {
		
		if (jsonEvent.has("event") && jsonEvent.getJSONObject("event").has("plugin") && jsonEvent.getJSONObject("event").getString("plugin").equals("janus.plugin.videoroom")) {
			
			JSONObject               dataJSON            = jsonEvent.getJSONObject("event").getJSONObject("data");
			VideoRoomPluginEventData roomPluginEventData = createVideoRoomPluginEventData(dataJSON);
			
			String                room                 = String.valueOf(dataJSON.getInt("room"));
			List<ParticipantPojo> roomParticipantsList = videoRoomMap.get(room);
			
			if (dataJSON.getString("event").equals("leaving")) {
				handleLeavingEvent(dataJSON, roomParticipantsList, room);
			} else if (dataJSON.getString("event").equals("joined")) {
				handleJoinedEvent(dataJSON, roomParticipantsList, room);
			}
			JanusEvent janusEvent = new JanusEvent();
			janusEvent.setEmitter(dataJSON.optString("emitter"));
			janusEvent.setSubtype(dataJSON.optInt("subtype"));
			janusEvent.setTimestamp(dataJSON.optLong("timestamp"));
			janusEvent.setType(dataJSON.optInt("type"));
			janusEvent.setHandle_id(dataJSON.optLong("handle_id"));
			janusEvent.setSession_id(dataJSON.optLong("session_id"));
			janusEvent.setOpaque_id(dataJSON.optString("opaque_id"));
			janusEvent.setEvent(roomPluginEventData);
			
			if (Janus.DB_ACCESS != null) {
				DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(janusEvent.trackInsert());
			}
		}
	}
	
	private VideoRoomPluginEventData createVideoRoomPluginEventData( JSONObject dataJSON ) {
		VideoRoomPluginEventData roomPluginEventData = new VideoRoomPluginEventData("janus.plugin.videoroom");
		roomPluginEventData.setRoom(dataJSON.optString("room"));
		roomPluginEventData.setDisplay(dataJSON.optString("display"));
		roomPluginEventData.setBitrate(dataJSON.optLong("bitrate"));
		roomPluginEventData.setId(dataJSON.optLong("id"));
		roomPluginEventData.setPrivate_id(dataJSON.optLong("private_id"));
		
		if (dataJSON.has("streams")) {
			JSONArray                        streams                         = dataJSON.getJSONArray("streams");
			VideoRoomPluginEventDataStream[] videoRoomPluginEventDataStreams = createVideoRoomEventDataStreams(streams);
			roomPluginEventData.setStream(videoRoomPluginEventDataStreams);
		}
		
		roomPluginEventData.setEvent(dataJSON.optString("event"));
		return roomPluginEventData;
	}
	
	private VideoRoomPluginEventDataStream[] createVideoRoomEventDataStreams( JSONArray streams ) {
		VideoRoomPluginEventDataStream[] videoRoomPluginEventDataStreams = new VideoRoomPluginEventDataStream[streams.length()];
		for (int i = 0; i < streams.length(); i++) {
			JSONObject                     stream                         = streams.getJSONObject(i);
			VideoRoomPluginEventDataStream videoRoomPluginEventDataStream = new VideoRoomPluginEventDataStream();
			videoRoomPluginEventDataStream.setMid(stream.optInt("mid"));
			videoRoomPluginEventDataStream.setMindex(stream.optInt("mindex"));
			videoRoomPluginEventDataStream.setCodec(stream.optString("codec"));
			videoRoomPluginEventDataStream.setType(stream.optString("type"));
			videoRoomPluginEventDataStreams[i] = videoRoomPluginEventDataStream;
		}
		return videoRoomPluginEventDataStreams;
	}
	
	private void handleLeavingEvent( JSONObject dataJSON, List<ParticipantPojo> roomParticipantsList, String room ) {
		if (roomParticipantsList == null) {
			return;
		}
		
		long            participantId      = dataJSON.optLong("id");
		ParticipantPojo leavingParticipant = findAndRemoveParticipant(roomParticipantsList, participantId);
		
		if (leavingParticipant != null) {
			onParticipantLeft(leavingParticipant.id(), leavingParticipant.display(), room);
		}
		
		if (roomParticipantsList.isEmpty()) {
			onRoomSessionEnded(room);
		}
	}
	
	private ParticipantPojo findAndRemoveParticipant( List<ParticipantPojo> roomParticipantsList, long participantId ) {
		ParticipantPojo participant = roomParticipantsList.stream()
				.filter(participantPojo -> participantPojo.id() == participantId)
				.findFirst()
				.orElse(null);
		
		if (participant != null) {
			roomParticipantsList.remove(participant);
		}
		
		return participant;
	}
	
	private void handleJoinedEvent( JSONObject dataJSON, List<ParticipantPojo> roomParticipantsList, String room ) {
		var id         = dataJSON.getLong("id");
		var display    = dataJSON.getString("display");
		var private_id = dataJSON.getLong("private_id");
		if (roomParticipantsList == null) {
			List<ParticipantPojo> participants = new ArrayList<>();
			participants.add(new ParticipantPojo(id, display, private_id));
			videoRoomMap.put(room, participants);
			onRoomSessionStarted(room, id, display);
			onParticipantJoined(id, display, room);
		} else {
			roomParticipantsList.add(new ParticipantPojo(id, display, private_id));
			videoRoomMap.put(room, roomParticipantsList);
			onParticipantJoined(id, display, room);
		}
	}
}
