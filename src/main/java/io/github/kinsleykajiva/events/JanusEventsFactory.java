package io.github.kinsleykajiva.events;


import io.github.kinsleykajiva.Janus;
import io.github.kinsleykajiva.cache.DBAccess;
import io.github.kinsleykajiva.cache.DatabaseConnection;
import io.github.kinsleykajiva.cache.mongodb.MongoConnection;
import io.github.kinsleykajiva.cache.mysql.MySqlConnection;
import io.github.kinsleykajiva.models.events.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * This class represents a factory for creating Janus events.
 */
public class JanusEventsFactory {
	
	private final        JSONObject                         jsonEvent;
	private final        JanusEventsEmissions               emissions;
	private static final Map<String, List<ParticipantPojo>> VIDEO_ROOM_MAP = new ConcurrentHashMap<>();
	
	/**
	 * Constructs a JanusEventsFactory object with the specified JSON event and emissions.
	 *
	 * @param jsonEvent The JSON event associated with the factory.
	 * @param emissions The JanusEventsEmissions object used for emitting events.
	 */
	public JanusEventsFactory( JSONObject jsonEvent, JanusEventsEmissions emissions ) {
		this.jsonEvent = jsonEvent;
		this.emissions = emissions;
	}
	
	/**
	 * Processes the event with ID 16.
	 * This method extracts the necessary information from the JSON event object
	 * and creates a JanusWebRTCStateEvent object to represent the event.
	 * If Janus.DB_ACCESS is not null, it inserts the event into the database using SQLBatchExec.
	 */
	public void processEvent16() {
		var jsonEventObj = jsonEvent.getJSONObject("event");
		var jevent = new JanusWebRTCStateEvent.Event(
				jsonEventObj.optString("ice", null),
				jsonEventObj.optInt("stream_id", 0),
				jsonEventObj.optInt("component_id", 0),
				jsonEventObj.optString("local-candidate", null),
				jsonEventObj.optString("remote-candidate", null)
		);
		System.out.println(jsonEvent.toString());
		System.out.println("99 + " + jsonEvent.optLong("timestamp", 0));
		var janusEvent = new JanusWebRTCStateEvent.Root(
				jsonEvent.optString("emitter", null),
				jsonEvent.optInt("type", 0),
				jsonEvent.optInt("subtype", 0),
				jsonEvent.optLong("timestamp", 0),
				jsonEvent.optLong("session_id", 0),
				jsonEvent.optLong("handle_id", 0),
				jsonEvent.optString("opaque_id", null),
				jevent
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusWebRTCStateEvent().trackInsert(janusEvent);
			for (Map.Entry<DatabaseConnection, List<String>> entry : insertSEL.entrySet()) {
				DatabaseConnection connection = entry.getKey();
				List<String>       command    = entry.getValue();
				if (connection instanceof MySqlConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MYSQL_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MYSQL_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				} else if (connection instanceof MongoConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MONGO_DB_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MONGO_DB_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				}
				
			}
		}
		
	}
	
	/**
	 * This method processes the given event. It extracts the necessary information from the JSON event object
	 * and creates a JanusHandleEvent.Root object to represent the event. If Janus.DB_ACCESS is not null, it
	 * inserts the event into the database using SQLBatchExec.
	 */
	public void processEvent2() {
		var jsonEventObj = jsonEvent.getJSONObject("event");
		var jevent = new JanusHandleEvent.Event(
				jsonEventObj.optString("name", null),
				jsonEventObj.optString("plugin", null),
				jsonEventObj.optString("opaque_id", null)
		);
		var janusEvent = new JanusHandleEvent.Root(
				jsonEvent.optString("emitter", null),
				jsonEvent.optInt("type", 0),
				jsonEvent.optLong("timestamp", 0),
				jsonEvent.optLong("session_id", 0),
				jsonEvent.optLong("handle_id", 0),
				jsonEvent.optString("opaque_id", null),
				jevent
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusHandleEvent().trackInsert(janusEvent);
			for (Map.Entry<DatabaseConnection, List<String>> entry : insertSEL.entrySet()) {
				DatabaseConnection connection = entry.getKey();
				List<String>       command    = entry.getValue();
				if (connection instanceof MySqlConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MYSQL_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MYSQL_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				} else if (connection instanceof MongoConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MONGO_DB_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MONGO_DB_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				}
				
			}
		}
	}
	
	/**
	 * Processes the event with ID 128.
	 * This method extracts the necessary information from the JSON event object
	 * and creates a JanusTransportOriginatedEvent object to represent the event.
	 * If Janus.DB_ACCESS is not null, it inserts the event into the database using SQLBatchExec.
	 */
	public void processEvent128() {
		var jsonEventObj = jsonEvent.getJSONObject("event");
		var jevent = new JanusTransportOriginatedEvent.Event(
				jsonEventObj.optString("transport", null),
				jsonEventObj.optString("id", null),
				new JanusTransportOriginatedEvent.Data(
						jsonEventObj.optString("event", null),
						jsonEventObj.optBoolean("admin_api", false),
						jsonEventObj.optString("ip", null),
						jsonEventObj.optInt("port", 0)
				)
		);
		var janusEvent = new JanusTransportOriginatedEvent.Root(
				jsonEvent.optString("emitter", null),
				jsonEvent.optInt("type", 0),
				jsonEvent.optLong("timestamp", 0),
				jevent
		);
		if (Janus.DB_ACCESS != null) {
			var commandMap = new JanusTransportOriginatedEvent().trackInsert(janusEvent);
			for (Map.Entry<DatabaseConnection, List<String>> entry : commandMap.entrySet()) {
				DatabaseConnection connection = entry.getKey();
				List<String>       command    = entry.getValue();
				if (connection instanceof MySqlConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MYSQL_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MYSQL_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				} else if (connection instanceof MongoConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MONGO_DB_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MONGO_DB_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				}
				
			}
		}
	}
	
	/**
	 * This method processes the event with ID 32.
	 * It extracts the necessary information from the JSON event object and creates a JanusMediaEvent object to represent the event.
	 * If Janus.DB_ACCESS is not null, it inserts the event into the database using SQLBatchExec.
	 */
	public void processEvent32() {
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
			var commandMap = new JanusMediaEvent().trackInsert(janusEvent);
			for (Map.Entry<DatabaseConnection, List<String>> entry : commandMap.entrySet()) {
				DatabaseConnection connection = entry.getKey();
				List<String>       command    = entry.getValue();
				if (connection instanceof MySqlConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MYSQL_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MYSQL_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				} else if (connection instanceof MongoConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MONGO_DB_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MONGO_DB_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				}
				
			}
		}
	}
	
	/**
	 * Processes the event with ID 8.
	 * <p>
	 * This method extracts the necessary information from the JSON event object and creates
	 * a JanusJSEPEvent.Root object to represent the event. If Janus.DB_ACCESS is not null,
	 * it inserts the event into the database using SQLBatchExec.
	 */
	public void processEvent8() {
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
			var commandMap = new JanusJSEPEvent().trackInsert(janusEvent);
			
			
			for (Map.Entry<DatabaseConnection, List<String>> entry : commandMap.entrySet()) {
				DatabaseConnection connection = entry.getKey();
				List<String>       command    = entry.getValue();
				if (connection instanceof MySqlConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MYSQL_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MYSQL_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				} else if (connection instanceof MongoConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MONGO_DB_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MONGO_DB_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				}
				
			}
			
			
		}
	}
	
	/**
	 * Processes the event with ID 1.
	 * <p>
	 * This method extracts the necessary information from the JSON event object and creates a JanusSessionEvent.Root
	 * object to represent the event. If Janus.DB_ACCESS is not null, it inserts the event into the database using
	 * SQLBatchExec.
	 */
	public void processEvent1() {
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
			for (Map.Entry<DatabaseConnection, List<String>> entry : insertSEL.entrySet()) {
				DatabaseConnection connection = entry.getKey();
				List<String>       command    = entry.getValue();
				if (connection instanceof MySqlConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MYSQL_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MYSQL_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				} else if (connection instanceof MongoConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MONGO_DB_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MONGO_DB_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				}
				
			}
		}
	}
	
	/**
	 * Processes the event with ID 256.
	 * <p>
	 * This method extracts the necessary information from the JSON event object and creates a JanusCoreEvent.Root object to represent the event.
	 * If Janus.DB_ACCESS is not null, it inserts the event into the database using SQLBatchExec.
	 * <p>
	 * This method does not return any value.
	 */
	public void processEvent256() {
		var jsonEventObj = jsonEvent.getJSONObject("event");
		var jevent = new JanusCoreEvent.Event(
				jsonEventObj.optString("status", null),
				new JanusCoreEvent.Info(
						jsonEventObj.optLong("sessions", 0),
						jsonEventObj.optLong("handles", 0),
						jsonEventObj.optLong("peerconnections", 0),
						jsonEventObj.optLong("stats-period", 0)
				)
		);
		var janusEvent = new JanusCoreEvent.Root(
				jsonEvent.optString("emitter", null),
				jsonEvent.optInt("type", 0),
				jsonEvent.optInt("subtype", 0),
				jsonEvent.optLong("timestamp", 0),
				jevent
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusCoreEvent().trackInsert(janusEvent);
			for (Map.Entry<DatabaseConnection, List<String>> entry : insertSEL.entrySet()) {
				DatabaseConnection connection = entry.getKey();
				List<String>       command    = entry.getValue();
				if (connection instanceof MySqlConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MYSQL_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MYSQL_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				} else if (connection instanceof MongoConnection) {
					if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MONGO_DB_CONNECTION_NAME)) {
						command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MONGO_DB_CONNECTION_NAME).executeDBActionCommand(insert));
					}
				}
				
			}
		}
	}
	
	/**
	 * Processes a video room event.
	 *
	 * @param jsonEvent the JSON object representing the event
	 */
	public void processVideoRoomEvent( JSONObject jsonEvent ) {
		
		if (jsonEvent.has("event") && jsonEvent.getJSONObject("event").has("plugin") && jsonEvent.getJSONObject("event").getString("plugin").equals("janus.plugin.videoroom")) {
			
			JSONObject               dataJSON            = jsonEvent.getJSONObject("event").getJSONObject("data");
			VideoRoomPluginEventData roomPluginEventData = createVideoRoomPluginEventData(dataJSON);
			
			String                room                 = String.valueOf(dataJSON.getInt("room"));
			List<ParticipantPojo> roomParticipantsList = VIDEO_ROOM_MAP.get(room);
			
			if (dataJSON.getString("event").equals("leaving")) {
				handleLeavingEvent(dataJSON, roomParticipantsList, room);
			} else if (dataJSON.getString("event").equals("joined")) {
				handleJoinedEvent(dataJSON, roomParticipantsList, room);
			}
			
			roomPluginEventData.setOpaque_id(jsonEvent.optString("opaque_id"));
			JanusEvent janusEvent = new JanusEvent();
			janusEvent.setEmitter(jsonEvent.optString("emitter"));
			janusEvent.setSubtype(0);
			janusEvent.setTimestamp(jsonEvent.optLong("timestamp"));
			janusEvent.setType(jsonEvent.optInt("type"));
			janusEvent.setHandle_id(jsonEvent.optLong("handle_id"));
			janusEvent.setSession_id(jsonEvent.optLong("session_id"));
			janusEvent.setOpaque_id(jsonEvent.optString("opaque_id"));
			janusEvent.setEvent(roomPluginEventData);
			
			if (Janus.DB_ACCESS != null) {
				var insertSEL = janusEvent.trackInsert();
				
				for (Map.Entry<DatabaseConnection, List<String>> entry : insertSEL.entrySet()) {
					DatabaseConnection connection = entry.getKey();
					List<String>       command    = entry.getValue();
					if (connection instanceof MySqlConnection) {
						if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MYSQL_CONNECTION_NAME)) {
							command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MYSQL_CONNECTION_NAME).executeDBActionCommand(insert));
						}
					} else if (connection instanceof MongoConnection) {
						if (Janus.DB_ACCESS.databaseConnectionExists(DBAccess.MONGO_DB_CONNECTION_NAME)) {
							command.forEach(insert -> Janus.DB_ACCESS.getDatabaseConnection(DBAccess.MONGO_DB_CONNECTION_NAME).executeDBActionCommand(insert));
						}
					}
					
				}
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
	/*private VideoRoomPluginEventDataStream[] createVideoRoomEventDataStreams( JSONArray streams ) {
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
	}*/
	
	private VideoRoomPluginEventDataStream[] createVideoRoomEventDataStreams( JSONArray streams ) {
		return IntStream.range(0, streams.length())
				.mapToObj(streams::getJSONObject)
				.map(this::createVideoRoomPluginEventDataStream)
				.toArray(VideoRoomPluginEventDataStream[]::new);
	}
	
	
	private VideoRoomPluginEventDataStream createVideoRoomPluginEventDataStream( JSONObject stream ) {
		VideoRoomPluginEventDataStream videoRoomPluginEventDataStream = new VideoRoomPluginEventDataStream();
		videoRoomPluginEventDataStream.setMid(stream.optInt("mid"));
		videoRoomPluginEventDataStream.setMindex(stream.optInt("mindex"));
		videoRoomPluginEventDataStream.setCodec(stream.optString("codec"));
		videoRoomPluginEventDataStream.setType(stream.optString("type"));
		return videoRoomPluginEventDataStream;
	}
	
	
	private void handleLeavingEvent( JSONObject dataJSON, List<ParticipantPojo> roomParticipantsList, String room ) {
		if (roomParticipantsList == null) {
			return;
		}
		
		long            participantId      = dataJSON.optLong("id");
		ParticipantPojo leavingParticipant = findAndRemoveParticipant(roomParticipantsList, participantId);
		
		if (leavingParticipant != null) {
			emissions.onParticipantLeft(leavingParticipant.id(), leavingParticipant.display(), room);
		}
		
		if (roomParticipantsList.isEmpty()) {
			emissions.onRoomSessionEnded(room);
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
			VIDEO_ROOM_MAP.put(room, participants);
			emissions.onRoomSessionStarted(room, id, display);
			emissions.onParticipantJoined(id, display, room);
		} else {
			roomParticipantsList.add(new ParticipantPojo(id, display, private_id));
			VIDEO_ROOM_MAP.put(room, roomParticipantsList);
			emissions.onParticipantJoined(id, display, room);
		}
	}
}
