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
			JSONObject jsonEvent = jsonArray.getJSONObject(i);
			int        type      = jsonEvent.getInt("type");
			if (type == 16) {
				var jsonEventObj = jsonEvent.getJSONObject("event");
				var jevent = new JanusWebRTCStateEvent.Event(
						jsonEventObj.optString("ice", null),
						jsonEventObj.optInt("stream_id", 0),
						jsonEventObj.optInt("component_id", 0),
						jsonEventObj.optString("local-candidate", null),
						jsonEventObj.optString("remote-candidate", null)
				);
				var janusEvent = new JanusWebRTCStateEvent.Root(
						jsonEventObj.optString("emitter", null),
						jsonEventObj.optInt("type", 0),
						jsonEventObj.optInt("subtype", 0),
						jsonEventObj.optLong("timestamp", 0),
						jsonEventObj.optLong("session_id", 0),
						jsonEventObj.optLong("handle_id", 0),
						jsonEventObj.optString("opaque_id", null),
						jevent
				);
				if (Janus.DB_ACCESS != null) {
					var insertSEL = new JanusWebRTCStateEvent().trackInsert(janusEvent);
					DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
				}
				
			}
			if (type == 2) {
				var jsonEventObj = jsonEvent.getJSONObject("event");
				var jevent = new JanusHandleEvent.Event(
						jsonEventObj.optString("name", null),
						jsonEventObj.optString("plugin", null),
						jsonEventObj.optString("opaque_id", null)
				);
				var janusEvent = new JanusHandleEvent.Root(
						jsonEventObj.optString("emitter", null),
						jsonEventObj.optInt("type", 0),
						jsonEventObj.optLong("timestamp", 0),
						jsonEventObj.optLong("session_id", 0),
						jsonEventObj.optLong("handle_id", 0),
						jsonEventObj.optString("opaque_id", null),
						jevent
				);
				if (Janus.DB_ACCESS != null) {
					var insertSEL = new JanusHandleEvent().trackInsert(janusEvent);
					DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
				}
			}
			if (type == 32) {
				var jsonEventObj = jsonEvent.getJSONObject("event");
				var jevent = new JanusMediaEvent.Event(
						jsonEventObj.optString("mid", null),
						jsonEventObj.optBoolean("receiving", false),
						jsonEventObj.optInt("receiving", 0),
						jsonEventObj.optString("media", null),
						jsonEventObj.optString("codec", null),
						jsonEventObj.optInt("base", 0),
						jsonEventObj.optInt("rtt", 0),
						jsonEventObj.optInt("lost", 0),
						jsonEventObj.optInt("lost_by_remote", 0),
						jsonEventObj.optInt("jitter_local", 0),
						jsonEventObj.optInt("jitter_remote", 0),
						jsonEventObj.optInt("in_link_quality", 0),
						jsonEventObj.optInt("in_media_link_quality", 0),
						jsonEventObj.optInt("out_link_quality", 0),
						jsonEventObj.optInt("out_media_link_quality", 0),
						jsonEventObj.optInt("packets_received", 0),
						jsonEventObj.optInt("packets_sent", 0),
						jsonEventObj.optInt("bytes_received", 0),
						jsonEventObj.optInt("bytes_sent", 0),
						jsonEventObj.optInt("bytes_received_lastsec", 0),
						jsonEventObj.optInt("bytes_sent_lastsec", 0),
						jsonEventObj.optInt("nacks_received", 0),
						jsonEventObj.optInt("nacks_sent", 0),
						jsonEventObj.optInt("retransmissions_received", 0)
				
				);
				var janusEvent = new JanusMediaEvent.Root(
						jsonEvent.getString("emitter"),
						jsonEvent.getInt("type"),
						jsonEvent.getInt("subtype"),
						jsonEvent.getLong("timestamp"),
						jsonEvent.getLong("session_id"),
						jsonEvent.getLong("handle_id"),
						jsonEvent.getString("opaque_id"),
						jevent
				);
				if (Janus.DB_ACCESS != null) {
					var insertSEL = new JanusMediaEvent().trackInsert(janusEvent);
					DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
				}
			}
			if (type == 8) {
				var jsep = jsonEvent.getJSONObject("event").has("jsep") ? new JanusJSEPEvent.Jsep(jsonEvent.getJSONObject("event").getJSONObject("jsep").optString("type", null),
						jsonEvent.getJSONObject("event").getJSONObject("jsep").optString("sdp", null)) : null;
				var janusJSEPEventEvent = new JanusJSEPEvent.Event(jsonEvent.getString("name"), jsep);
				var janusEvent = new JanusJSEPEvent.Root(
						jsonEvent.optString("emitter", null),
						jsonEvent.optInt("type", 0),
						jsonEvent.optLong("timestamp", 0),
						jsonEvent.optLong("session_id", 0),
						jsonEvent.optLong("handle_id", 0),
						jsonEvent.optString("opaque_id", null),
						janusJSEPEventEvent
				);
				if (Janus.DB_ACCESS != null) {
					var insertSEL = new JanusJSEPEvent().trackInsert(janusEvent);
					DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
				}
			}
			if (type == 1) {
				var janusEvent = new JanusSessionEvent.Root(
						jsonEvent.optString("emitter", null),
						jsonEvent.optInt("type", 0),
						jsonEvent.optLong("timestamp", 0),
						jsonEvent.optLong("session_id", 0),
						new JanusSessionEvent.Event(
								jsonEvent.getString("name"),
								jsonEvent.has("transport") ? new JanusSessionEvent.Transport(
										jsonEvent.getJSONObject("transport").optString("transport", null),
										jsonEvent.getJSONObject("transport").optLong("id", 0)
								) : null
						)
				);
				if (Janus.DB_ACCESS != null) {
					var insertSEL = new JanusSessionEvent().trackInsert(janusEvent);
					DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
				}
			}
			if (type == 64) {
				if (jsonEvent.has("event") && jsonEvent.getJSONObject("event").has("plugin")
						&& jsonEvent.getJSONObject("event").getString("plugin").equals("janus.plugin.videoroom")) {
					executorService.submit(() -> processVideoRoomEvent(jsonEvent));
				}
				
			}
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
