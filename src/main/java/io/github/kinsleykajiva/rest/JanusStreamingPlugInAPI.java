package io.github.kinsleykajiva.rest;


import io.github.kinsleykajiva.JanusPlugins;
import io.github.kinsleykajiva.SdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JanusStreamingPlugInAPI {
	static  Logger             log = Logger.getLogger(JanusStreamingPlugInAPI.class.getName());
	private JanusRestApiClient janusRestApiClient;
	
	public JanusStreamingPlugInAPI( @NotNull JanusRestApiClient janusRestApiClient ) {
		this.janusRestApiClient = janusRestApiClient;
	}
	/**
	 * Creates a new mounting point in the Janus WebRTC server for streaming purposes.
	 *
	 * @param type               The type of the mounting point (e.g., "rtp or live or ondemand or rtsp" ).
	 * @param name               The name of the mounting point (nullable).
	 * @param description        A description for the mounting point (nullable).
	 * @param metadata           Additional metadata associated with the mounting point (nullable).
	 * @param secret             The secret token required to authenticate the creation request (nullable).
	 * @param pin                A Personal Identification Number (PIN) for accessing the mounting point (nullable).
	 * @param isPrivate          Indicates whether the mounting point is private or public.
	 * @param permanent          Indicates whether the mounting point should be permanent or temporary.
	 * @param mediaStringJsonArray A JSON array string specifying the media configuration for the mounting point.
	 *                              Example: "[{"type": "audio"}, {"type": "video"}]"
	 * @return A JSONObject containing the response from the Janus server after attempting to create the mounting point.
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
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "create" or "error",
	 *       "id": "&lt;mounting_point_id&gt;",
	 *       "result": "success" or "error",
	 *       "error": "&lt;error_message&gt;"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If the mounting point creation is successful, "janus" will be "success" and "streaming" will be "create".
	 * The "id" field will contain the unique identifier for the created mounting point.
	 * If an error occurs during the creation, "janus" will be "error", "streaming" will be "error", and the "error" field may contain additional details.
	 */
	public JSONObject createMountingPoint( @NotNull String type, @Nullable String name,
	                                       @Nullable String description, @Nullable String metadata,
	                                       @Nullable String secret, @Nullable String pin,
	                                       boolean isPrivate, boolean permanent, @NotNull String mediaStringJsonArray ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_STREAMING);
		
		final var supportedTYpe  = new String[]{ "rtp","live","ondemand","rtsp" };
		if(Arrays.asList(supportedTYpe).contains(type)){
			return new JSONObject().put("error","type not supported . Supported types are: " + Arrays.toString(supportedTYpe));
		}
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		json.put("body", new JSONObject()
				.put("request", "create")
				.put("id", SdkUtils.uniqueIDGenerator("", 20) + SdkUtils.IdGenerator())
				.put("name", name)
				.put("metadata", metadata)
				.put("type", type)
				.put("secret", secret)
				.put("pin", pin)
				.put("permanent", permanent)
				.put("description", description)
				.put("is_private", isPrivate)
				.put("media", new JSONArray(mediaStringJsonArray))
				.put("admin_key", janusRestApiClient.getJanusConfiguration().adminKey())
		);
		try {
			var response = janusRestApiClient.makePostRequest(json);
			return new JSONObject(response);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to create mounting point: " + e.getMessage(), e);
		}
		return new JSONObject();
		
	}
	
	
	/**
	 * Edits an existing mounting point in the Janus WebRTC server for streaming purposes.
	 *
	 * @param id                The unique identifier of the mounting point to be edited.
	 * @param edited_event      Indicates whether an "edited" event should be triggered.
	 * @param description       The new description for the mounting point (nullable).
	 * @param metadata          The new metadata for the mounting point (nullable).
	 * @param secret            The new secret token required to authenticate the edit request (nullable).
	 * @param pin               The new Personal Identification Number (PIN) for accessing the mounting point (nullable).
	 * @param isPrivate         Indicates whether the mounting point should be private or public.
	 * @param permanent         Indicates whether the mounting point should be permanent or temporary.
	 * @return A JSONObject containing the response from the Janus server after attempting to edit the mounting point.
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
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "edit" or "error",
	 *       "id": "&lt;mounting_point_id&gt;",
	 *       "result": "success" or "error",
	 *       "error": "&lt;error_message&gt;"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If the mounting point edit is successful, "janus" will be "success" and "streaming" will be "edit".
	 * The "id" field will contain the unique identifier of the edited mounting point.
	 * If an error occurs during the edit, "janus" will be "error", "streaming" will be "error", and the "error" field may contain additional details.
	 */
	public JSONObject editMountingPoint( @NotNull String id, boolean edited_event,
	                                     @Nullable String description, @Nullable String metadata,
	                                     @Nullable String secret, @Nullable String pin,
	                                     boolean isPrivate, boolean permanent ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_STREAMING);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		json.put("body", new JSONObject()
				.put("request", "edit")
				.put("id", id)
				.put("edited_event", edited_event)
				.put("new_metadata", metadata)
				.put("new_secret", secret)
				.put("new_pin", pin)
				.put("permanent", permanent)
				.put("new_description", description)
				.put("new_is_private", isPrivate)
				.put("admin_key", janusRestApiClient.getJanusConfiguration().adminKey())
		);
		try {
			var response = janusRestApiClient.makePostRequest(json);
			return new JSONObject(response);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to create mounting point: " + e.getMessage(), e);
		}
		return new JSONObject();
		
	}
	
	/**
	 * Deletes an existing mounting point in the Janus WebRTC server for streaming purposes.
	 *
	 * @param id        The unique identifier of the mounting point to be deleted.
	 * @param secret    The secret token required to authenticate the deletion request (nullable).
	 * @param permanent Indicates whether the mounting point should be permanently deleted.
	 * @return A JSONObject containing the response from the Janus server after attempting to delete the mounting point.
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
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "destroy" or "error",
	 *       "id": "&lt;mounting_point_id&gt;",
	 *       "result": "success" or "error",
	 *       "error": "&lt;error_message&gt;"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If the mounting point deletion is successful, "janus" will be "success" and "streaming" will be "destroy".
	 * The "id" field will contain the unique identifier of the deleted mounting point.
	 * If an error occurs during the deletion, "janus" will be "error", "streaming" will be "error", and the "error" field may contain additional details.
	 */
	public JSONObject deleteMountingPoint( @NotNull String id, @Nullable String secret, boolean permanent ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_STREAMING);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		json.put("body", new JSONObject()
				.put("request", "destroy")
				.put("id", id)
				.put("secret", secret)
				.put("permanent", permanent)
				.put("admin_key", janusRestApiClient.getJanusConfiguration().adminKey())
		);
		try {
			var response = janusRestApiClient.makePostRequest(json);
			return new JSONObject(response);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to destroying mounting point: " + e.getMessage(), e);
		}
		return new JSONObject();
		
	}
	/**
	 * Enables an existing mounting point in the Janus WebRTC server for streaming purposes.
	 *
	 * @param id     The unique identifier of the mounting point to be enabled.
	 * @param secret The secret token required to authenticate the enablement request (nullable).
	 * @return A JSONObject containing the response from the Janus server after attempting to enable the mounting point.
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
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "event",
	 *       "result": "enabled" or "error",
	 *       "id": "&lt;mounting_point_id&gt;",
	 *       "error": "&lt;error_message&gt;"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If the mounting point enablement is successful, "janus" will be "success", "streaming" will be "event", and "result" will be "enabled".
	 * The "id" field will contain the unique identifier of the enabled mounting point.
	 * If an error occurs during the enablement, "janus" will be "error", "streaming" will be "event", "result" will be "error", and the "error" field may contain additional details.
	 */
	public JSONObject enableMountingPoint( @NotNull String id, @Nullable String secret ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_STREAMING);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		json.put("body", new JSONObject()
				.put("request", "enable")
				.put("id", id)
				.put("secret", secret)
				.put("admin_key", janusRestApiClient.getJanusConfiguration().adminKey())
		);
		try {
			var response = janusRestApiClient.makePostRequest(json);
			return new JSONObject(response);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to enable mounting point: " + e.getMessage(), e);
		}
		return new JSONObject();
		
	}
	
	
	/**
	 * Kicks all participants from an existing mounting point in the Janus WebRTC server.
	 *
	 * @param id     The unique identifier of the mounting point from which participants will be kicked.
	 * @param secret The secret token required to authenticate the kick-all request (nullable).
	 * @return A JSONObject containing the response from the Janus server after attempting to kick all participants.
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
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "event",
	 *       "result": "kicked_all" or "error",
	 *       "id": "&lt;mounting_point_id&gt;",
	 *       "error": "&lt;error_message&gt;"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If kicking all participants is successful, "janus" will be "success", "streaming" will be "event", and "result" will be "kicked_all".
	 * The "id" field will contain the unique identifier of the affected mounting point.
	 * If an error occurs during the kick-all operation, "janus" will be "error", "streaming" will be "event", "result" will be "error", and the "error" field may contain additional details.
	 */
	public JSONObject kickAllMountingPoint( @NotNull String id, @Nullable String secret ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_STREAMING);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		json.put("body", new JSONObject()
				.put("request", "kick_all")
				.put("id", id)
				.put("secret", secret)
				.put("admin_key", janusRestApiClient.getJanusConfiguration().adminKey())
		);
		try {
			var response = janusRestApiClient.makePostRequest(json);
			return new JSONObject(response);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to enable mounting point: " + e.getMessage(), e);
		}
		return new JSONObject();
		
	}
	
	/**
	 * Initiates recording for a specific mounting point in the Janus WebRTC server.
	 *
	 * @param id                 The unique identifier of the mounting point to start recording.
	 * @param secret             The secret token required to authenticate the recording request (nullable).
	 * @param mediaStringJsonArray A JSON array string specifying the media types to be recorded.
	 *                             Example: '["audio", "video"]'
	 * @return A JSONObject containing the response from the Janus server after attempting to start recording.
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
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "event",
	 *       "result": "recording_started" or "error",
	 *       "id": "&lt;mounting_point_id&gt;",
	 *       "error": "&lt;error_message&gt;"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If recording is successfully started, "janus" will be "success", "streaming" will be "event", and "result" will be "recording_started".
	 * The "id" field will contain the unique identifier of the recording mounting point.
	 * If an error occurs during the recording initiation, "janus" will be "error", "streaming" will be "event", "result" will be "error", and the "error" field may contain additional details.
	 */
	public JSONObject recordingMountingPoint( @NotNull String id, @Nullable String secret,@NotNull String  mediaStringJsonArray ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_STREAMING);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		json.put("body", new JSONObject()
				.put("request", "recording")
				.put("action", "start")
				.put("id", id)
				.put("secret", secret)
				.put("media", new JSONArray(mediaStringJsonArray))
				.put("admin_key", janusRestApiClient.getJanusConfiguration().adminKey())
		);
		try {
			var response = janusRestApiClient.makePostRequest(json);
			return new JSONObject(response);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to enable mounting point: " + e.getMessage(), e);
		}
		return new JSONObject();
		
	}
	
	
	/**
	 * Stops recording for a specific mounting point in the Janus WebRTC server.
	 *
	 * @param id                 The unique identifier of the mounting point to stop recording.
	 * @param secret             The secret token required to authenticate the recording stop request (nullable).
	 * @param mediaStringJsonArray A JSON array string specifying the media types to stop recording.
	 *                             Example: '["audio", "video"]'
	 * @return A JSONObject containing the response from the Janus server after attempting to stop recording.
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
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "event",
	 *       "result": "recording_stopped" or "error",
	 *       "id": "&lt;mounting_point_id&gt;",
	 *       "error": "&lt;error_message&gt;"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If recording is successfully stopped, "janus" will be "success", "streaming" will be "event", and "result" will be "recording_stopped".
	 * The "id" field will contain the unique identifier of the recording mounting point.
	 * If an error occurs during the recording stop, "janus" will be "error", "streaming" will be "event", "result" will be "error", and the "error" field may contain additional details.
	 */
	public JSONObject stopRecordingMountingPoint( @NotNull String id, @Nullable String secret,@NotNull String  mediaStringJsonArray ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_STREAMING);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		json.put("body", new JSONObject()
				.put("request", "recording")
				.put("action", "stop")
				.put("id", id)
				.put("secret", secret)
				.put("media", new JSONArray(mediaStringJsonArray))
				.put("admin_key", janusRestApiClient.getJanusConfiguration().adminKey())
		);
		try {
			var response = janusRestApiClient.makePostRequest(json);
			return new JSONObject(response);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to enable mounting point: " + e.getMessage(), e);
		}
		return new JSONObject();
		
	}
	/**
	 * Retrieves information about a specific mounting point in the Janus WebRTC server.
	 *
	 * @param id     The unique identifier of the mounting point for which to retrieve information.
	 * @param secret The secret token required to authenticate the information retrieval request (nullable).
	 * @return A JSONObject containing the response from the Janus server after attempting to retrieve information.
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
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "info",
	 *       "result": "ok" or "error",
	 *       "id": "&lt;mounting_point_id&gt;",
	 *       "description": "&lt;mounting_point_description&gt;",
	 *       "metadata": "&lt;mounting_point_metadata&gt;",
	 *       "is_private": true or false,
	 *       "permanent": true or false,
	 *       "recording": true or false,
	 *       "rec_dir": "&lt;recording_directory&gt;",
	 *       "pin": "&lt;pin&gt;",
	 *       "api_key": "&lt;api_key&gt;",
	 *       "admin_key": "&lt;admin_key&gt;"
	 *     }
	 *   }
	 * }
	 * &lt;/code&gt;
	 * &lt;/p&gt;
	 * If information retrieval is successful, "janus" will be "success", "streaming" will be "info", and "result" will be "ok".
	 * The response will contain details about the specified mounting point such as its description, metadata, privacy status, permanence, recording status, recording directory, PIN, API key, and admin key.
	 * If an error occurs during information retrieval, "janus" will be "error", "streaming" will be "info", "result" will be "error", and the "error" field may contain additional details.
	 */
	public JSONObject infoMountingPoint(@NotNull String id, @Nullable String secret) {
		// Setting up Janus session and attaching streaming plugin.
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_STREAMING);
		
		// Constructing the JSON message for the information retrieval request.
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		json.put("body", new JSONObject()
				.put("request", "info")
				.put("id", id)
				.put("secret", secret)
				.put("admin_key", janusRestApiClient.getJanusConfiguration().adminKey())
		);
		
		try {
			// Making the POST request to retrieve information about the mounting point.
			var response = janusRestApiClient.makePostRequest(json);
			return new JSONObject(response);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to retrieve information about the mounting point: " + e.getMessage(), e);
		}
		
		// Return an empty JSONObject if an error occurs during the information retrieval.
		return new JSONObject();
	}
	
	
}
