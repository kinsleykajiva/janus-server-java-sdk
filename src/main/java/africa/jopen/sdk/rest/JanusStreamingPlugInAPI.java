package africa.jopen.sdk.rest;

import africa.jopen.sdk.JanusPlugins;
import africa.jopen.sdk.SdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

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
	 * @param type               The type of the mounting point (e.g., "rtp" or "rtmp").
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
	 * <p>
	 * <code>
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "<transaction_id>",
	 *   "session_id": <session_id>,
	 *   "handle_id": <handle_id>,
	 *   "sender": <sender_id>,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "create" or "error",
	 *       "id": "<mounting_point_id>",
	 *       "result": "success" or "error",
	 *       "error": "<error_message>"
	 *     }
	 *   }
	 * }
	 * </code>
	 * </p>
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
	 * <p>
	 * <code>
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "<transaction_id>",
	 *   "session_id": <session_id>,
	 *   "handle_id": <handle_id>,
	 *   "sender": <sender_id>,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "edit" or "error",
	 *       "id": "<mounting_point_id>",
	 *       "result": "success" or "error",
	 *       "error": "<error_message>"
	 *     }
	 *   }
	 * }
	 * </code>
	 * </p>
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
	 * <p>
	 * <code>
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "<transaction_id>",
	 *   "session_id": <session_id>,
	 *   "handle_id": <handle_id>,
	 *   "sender": <sender_id>,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "destroy" or "error",
	 *       "id": "<mounting_point_id>",
	 *       "result": "success" or "error",
	 *       "error": "<error_message>"
	 *     }
	 *   }
	 * }
	 * </code>
	 * </p>
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
	 * <p>
	 * <code>
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "<transaction_id>",
	 *   "session_id": <session_id>,
	 *   "handle_id": <handle_id>,
	 *   "sender": <sender_id>,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "event",
	 *       "result": "enabled" or "error",
	 *       "id": "<mounting_point_id>",
	 *       "error": "<error_message>"
	 *     }
	 *   }
	 * }
	 * </code>
	 * </p>
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
	 * <p>
	 * <code>
	 * {
	 *   "janus": "success" or "error",
	 *   "transaction": "<transaction_id>",
	 *   "session_id": <session_id>,
	 *   "handle_id": <handle_id>,
	 *   "sender": <sender_id>,
	 *   "plugindata": {
	 *     "plugin": "janus.plugin.streaming",
	 *     "data": {
	 *       "streaming": "event",
	 *       "result": "kicked_all" or "error",
	 *       "id": "<mounting_point_id>",
	 *       "error": "<error_message>"
	 *     }
	 *   }
	 * }
	 * </code>
	 * </p>
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
	
	
}
