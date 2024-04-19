package io.github.kinsleykajiva.rest;


import io.github.kinsleykajiva.utils.JanusPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JanusVideoRoomPlugInAPI {
	static Logger             log = Logger.getLogger(JanusVideoRoomPlugInAPI.class.getName());
	private    JanusRestApiClient janusRestApiClient;
	
	public JanusVideoRoomPlugInAPI(@NotNull JanusRestApiClient janusRestApiClient ) {
		this.janusRestApiClient = janusRestApiClient;
	}
	
	/**
	 * Deletes a video room in the Janus WebRTC server.
	 *
	 * @param roomId  The identifier of the room to be deleted (string or integer).
	 * @param secret  The secret token required to authenticate the deletion request.
	 * @return A JSONObject containing the response from the Janus server after attempting to delete the room.
	 * The response will have the structure:
	 * &lt;p&gt;
	 * &lt;code&gt;
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "&lt;transaction_id&gt;",
	 *   "session_id": &lt;session_id&gt;,
	 *   "handle_id": &lt;handle_id&gt;,
	 *   "sender": &lt;sender_id&gt;,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.videoroom",
	 *     "data": {
	 *       "videoroom": "destroyed"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If the room deletion is successful, "janus" will be "success" and "videoroom" will be "destroyed".
	 * If an error occurs, "janus" will be "error" and additional details may be available in the "error" field.
	 * If the specified room does not exist, null will be returned.
	 * @see #checkIfVideoRoomExistsBoolCheck(String)
	 */
	public JSONObject deleteRoom( String roomId ,String secret) {
		if (!checkIfVideoRoomExistsBoolCheck(roomId)) {
			// is the room does not exist
			return null;
		}
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_VIDEO_ROOM);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		
		for (String idType : new String[]{"integer", "string"}) {
			try {
				json.put("body",
						new JSONObject()
								.put("request", "destroy")
								.put("secret", secret)
								.put("permanent", true)
								.put("room", idType.equals("integer") ? Integer.parseInt(roomId) : roomId)
				);
				
				String response = janusRestApiClient.makePostRequest(json);
				JSONObject responseObject = new JSONObject(response);
				
				
				return responseObject;
			} catch (Exception e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return new JSONObject();
	}
	
	/**
	 * Checks if a video room exists in the Janus WebRTC server and returns a boolean indicating its existence.
	 *
	 * @param roomId The identifier of the room to check for existence (string or integer).
	 * @return {@code true} if the room exists, {@code false} otherwise.
	 * @see #checkIfVideoRoomExists(String)
	 */
	public boolean checkIfVideoRoomExistsBoolCheck( String roomId ) {
		var responseObject = checkIfVideoRoomExists(roomId);
		if (responseObject == null) {
			return false;
		}
		
		return responseObject.getString("janus").equals("success")
				&& responseObject.has("plugindata") && responseObject.getJSONObject("plugindata").getJSONObject("data").has("videoroom")
				&& responseObject.getJSONObject("plugindata").getJSONObject("data").getString("videoroom").equals("success")
				&& responseObject.getJSONObject("plugindata").getJSONObject("data").getBoolean("exists");
	}
	
	
	/**
	 * Creates a new video room in the Janus WebRTC server.
	 *
	 * @param roomId      The identifier for the new room (string or integer).
	 * @param description A description for the new room (nullable).
	 * @param pin         A PIN (Personal Identification Number) for accessing the room (nullable).
	 * @param secret      The secret token required to authenticate the creation request (nullable).
	 * @param publishers  The maximum number of publishers allowed in the room.
	 *                    If the specified value is less than 1, it will be set to 1.
	 * @param permanent   Indicates whether the room should be permanent or temporary.
	 * @param record      Indicates whether the room should record the sessions.
	 * @param rec_dir     The directory path for storing recorded sessions (nullable).
	 * @return A JSONObject containing the response from the Janus server after attempting to create the room.
	 * The response will have the structure:
	 * &lt;p&gt;
	 * &lt;code&gt;
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "&lt;transaction_id&gt;",
	 *   "session_id": &lt;session_id&gt;,
	 *   "handle_id": &lt;handle_id&gt;,
	 *   "sender": &lt;sender_id&gt;,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.videoroom",
	 *     "data": {
	 *       "videoroom": "created"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If the room creation is successful, "janus" will be "success" and "videoroom" will be "created".
	 * If an error occurs, "janus" will be "error" and additional details may be available in the "error" field.
	 * If the specified room already exists, null will be returned.
	 * @see #checkIfVideoRoomExistsBoolCheck(String)
	 */
	public JSONObject createJanusRoom( @NotNull String roomId, @Nullable String description,
	                                   @Nullable String pin, @Nullable String secret, int publishers,
	                                   boolean permanent, boolean record, @Nullable String rec_dir ) {
		if (checkIfVideoRoomExistsBoolCheck(roomId)) {
			return null;
		}
		
		if (publishers < 1) {
			publishers = 1;
		}
		
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_VIDEO_ROOM);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		
		try {
			// We have a problem here we don't know if Video room is configured to use string room id or integer value only .
			
			for (String idType : new String[]{ "integer", "string" }) {
				json.put("body", new JSONObject()
						.put("request", "create")
						.put("description", description)
						.put("pin", pin)
						.put("secret", secret)
						.put("publishers", publishers)
						.put("permanent", permanent)
						.put("record", record)
						.put("rec_dir", rec_dir)
						.put("room", idType.equals("integer") ? Integer.parseInt(roomId) : roomId)
				);
				
				String response = janusRestApiClient.makePostRequest(json);
				System.out.println("xxxx=&gt; " + response);
				JSONObject responseObject = new JSONObject(response);
				
				if (responseObject.getString("janus").equals("success")) {
					JSONObject plugindata = responseObject.getJSONObject("plugindata");
					JSONObject data       = plugindata.getJSONObject("data");
					if (data.has("videoroom") && data.getString("videoroom").equals("created")) {
						return responseObject;
					}
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		return new JSONObject();
	}
	
	
	/**
	 * Checks if a video room exists in the Janus WebRTC server.
	 *
	 * @param roomId The identifier of the room to check for existence (string or integer).
	 * @return A JSONObject containing the response from the Janus server after attempting to check the room's existence.
	 * The response will have the structure:
	 * &lt;p&gt;
	 * &lt;code&gt;
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "&lt;transaction_id&gt;",
	 *   "session_id": &lt;session_id&gt;,
	 *   "handle_id": &lt;handle_id&gt;,
	 *   "sender": &lt;sender_id&gt;,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.videoroom",
	 *     "data": {
	 *       "videoroom": "exists" or "error"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If the room exists, "janus" will be "success" and "videoroom" will be "exists".
	 * If the room does not exist, "janus" will be "success" and "videoroom" will be "error".
	 * If an error occurs during the check, "janus" will be "error" and additional details may be available in the "error" field.
	 * @see #checkIfVideoRoomExistsBoolCheck(String)
	 */
	public JSONObject checkIfVideoRoomExists( String roomId ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_VIDEO_ROOM);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		
		// We have a problem here we don't know if Video room is configured to use string room id or integer value only .
		// because of this one of the types will cause an exception if the type use is not accepted by the plugin.
		// so we have to make two attempts one with string and/or the other with integer.
		
		for (String idType : new String[]{ "integer", "string" }) {
			try {
				json.put("body",
						new JSONObject()
								.put("request", "exists")
								.put("room", idType.equals("integer") ? Integer.parseInt(roomId) : roomId)
				);
				
				String     response       = janusRestApiClient.makePostRequest(json);
				JSONObject responseObject = new JSONObject(response);
				
				if (responseObject.getString("janus").equals("success")
						&& responseObject.has("plugindata") && responseObject.getJSONObject("plugindata").getJSONObject("data").has("error_code")) {
					return new JSONObject();
				}
				return responseObject;
			} catch (Exception e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		
		return new JSONObject();
	}
	
	/**
	 * Retrieves the list of existing video rooms from the Janus WebRTC server.
	 *
	 * @return A JSONObject containing the response from the Janus server after attempting to retrieve the list of video rooms.
	 * The response will have the structure:
	 * &lt;p&gt;
	 * &lt;code&gt;
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "&lt;transaction_id&gt;",
	 *   "session_id": &lt;session_id&gt;,
	 *   "handle_id": &lt;handle_id&gt;,
	 *   "sender": &lt;sender_id&gt;,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.videoroom",
	 *     "data": {
	 *       "videoroom": "list" or "error",
	 *       "rooms": [
	 *         {
	 *           "room": &lt;room_id&gt;,
	 *           "description": "&lt;room_description&gt;",
	 *           "audiocodec": "&lt;codec eg opus, pcma, pcmu, isac, g722, g726, g711, speex, silk, isac16k, isac32k, isac48k, isac128k, isac256k, ilbc, siren7, siren14, silk12, silk16, silk24, silk48&gt;",
	 *           "videocodec": "&lt;codec eg vp8, h264&gt;",
	 *           "fir_freq": "&lt;fir_freq&gt;",
	 *           "num_participants": "&lt;fir_freq&gt;",
	 *           "num_participants": &lt;number_of_participants&gt;,
	 *           "is_private": true or false,
	 *           "audiolevel_event": true or false,
	 *           "require_e2ee": true or false,
	 *           "playoutdelay_ext": true or false,
	 *           "videoorient_ext": true or false,
	 *           "notify_joining": true or false,
	 *           "require_pvtid": true or false,
	 *           "bitrate": &lt;bitrate&gt;,
	 *           "bitrate_audio": &lt;audio_bitrate&gt;,
	 *           "bitrate_video": &lt;video_bitrate&gt;,
	 *           "created": &lt;timestamp&gt;,
	 *           "permanent": true or false,
	 *           "record": true or false,
	 *           "rec_dir": "&lt;recording_directory&gt;"
	 *           ...
	 *         },
	 *         ...
	 *       ]
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If the list retrieval is successful, "janus" will be "success" and "videoroom" will be "list".
	 * If an error occurs during the retrieval, "janus" will be "error" and additional details may be available in the "error" field.
	 */
	public JSONObject getRooms() {
		// Setting up Janus session and attaching video room plugin.
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_VIDEO_ROOM);
		
		// Constructing the JSON message for the list retrieval request.
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		json.put("body",
				new JSONObject()
						.put("request", "list")
		);
		
		try {
			// Making the POST request to retrieve the list of video rooms.
			var response = janusRestApiClient.makePostRequest(json);
			return new JSONObject(response);
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		// Return an empty JSONObject if an error occurs during the list retrieval.
		return new JSONObject();
	}
	
	
	/**
	 * Retrieves the list of participants in a video room from the Janus WebRTC server.
	 *
	 * @param roomId The identifier of the room for which to retrieve the list of participants (string or integer).
	 * @return A JSONObject containing the response from the Janus server after attempting to retrieve the list of room participants.
	 * The response will have the structure:
	 * &lt;p&gt;
	 * &lt;code&gt;
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "&lt;transaction_id&gt;",
	 *   "session_id": &lt;session_id&gt;,
	 *   "handle_id": &lt;handle_id&gt;,
	 *   "sender": &lt;sender_id&gt;,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.videoroom",
	 *     "data": {
	 *       "videoroom": "listparticipants" or "error",
	 *       "participants": [
	 *         {
	 *           "id": &lt;participant_id&gt;,
	 *           "display": "&lt;display_name&gt;",
	 *           "audio_codec": "&lt;audio_codec&gt;",
	 *           "video_codec": "&lt;video_codec&gt;",
	 *           "feed": &lt;feed_id&gt;,
	 *           ...
	 *         },
	 *         ...
	 *       ]
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If the participant list retrieval is successful, "janus" will be "success" and "videoroom" will be "listparticipants".
	 * If an error occurs during the retrieval, "janus" will be "error" and additional details may be available in the "error" field.
	 */
	public JSONObject listRoomParticipants( @NotNull final   String roomId ) {
		// Setting up Janus session and attaching video room plugin.
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_VIDEO_ROOM);
		
		// Constructing the JSON message for the list retrieval request.
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		
		for (String idType : new String[]{ "integer", "string" }) {
			try {
				json.put("body",
						new JSONObject()
								.put("request", "listparticipants")
								.put("room", idType.equals("integer") ? Integer.parseInt(roomId) : roomId)
				);
				// Making the POST request to retrieve the list of video rooms.
				var response = janusRestApiClient.makePostRequest(json);
				return new JSONObject(response);
			} catch (Exception e) {
				// Log or handle the exception if needed.
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return new JSONObject();
	}
	
	
	
}
