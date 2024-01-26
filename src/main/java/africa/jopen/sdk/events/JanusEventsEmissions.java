package africa.jopen.sdk.events;


import africa.jopen.sdk.SdkUtils;
import africa.jopen.sdk.models.events.ParticipantPojo;
import africa.jopen.sdk.models.events.VideoRoomPluginEventData;
import africa.jopen.sdk.models.events.VideoRoomPluginEventDataStream;
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
default void consumeEventAsync(String event) {
    JSONArray jsonArray = SdkUtils.isJsonArray(event) ? new JSONArray(event) : new JSONArray().put(new JSONObject(event));
    for (int i = 0; i < jsonArray.length(); i++) {
	    int finalI = i;
	    executorService.submit(() -> {
            JanusEventsFactory janusEventsFactory = new JanusEventsFactory(jsonArray.getJSONObject(finalI) , this);
            JSONObject jsonEvent = jsonArray.getJSONObject(finalI);
            int type = jsonEvent.getInt("type");
            switch (type) {
                case 256:
                    janusEventsFactory.processEvent256();
                    break;
                case 128:
                    janusEventsFactory.processEvent128();
                    break;
                case 16:
                    janusEventsFactory.processEvent16();
                    break;
                case 2:
                    janusEventsFactory.processEvent2();
                    break;
                case 32:
                    janusEventsFactory.processEvent32();
                    break;
                case 8:
                    janusEventsFactory.processEvent8();
                    break;
                case 1:
                    janusEventsFactory.processEvent1();
                    break;
                case 64:
                    if (jsonEvent.has("event") && jsonEvent.getJSONObject("event").has("plugin")
                        && jsonEvent.getJSONObject("event").getString("plugin").equals("janus.plugin.videoroom")) {
                        processVideoRoomEvent(jsonEvent);
                    }
                    break;
                default:
                    // Handle the case where no other cases match, if necessary
                    break;
            }
        });
    }
}
	
	private void processVideoRoomEvent( JSONObject jsonEvent ) {
		JSONObject               dataJSON            = jsonEvent.getJSONObject("event").getJSONObject("data");
		VideoRoomPluginEventData roomPluginEventData = createVideoRoomPluginEventData(dataJSON);
		
		String                room                 = String.valueOf(dataJSON.getInt("room"));
		List<ParticipantPojo> roomParticipantsList = videoRoomMap.get(room);
		
		if (dataJSON.getString("event").equals("leaving")) {
			handleLeavingEvent(dataJSON, roomParticipantsList, room);
		} else if (dataJSON.getString("event").equals("joined")) {
			handleJoinedEvent(dataJSON, roomParticipantsList, room);
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
