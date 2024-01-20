package africa.jopen.sdk.rest;

import africa.jopen.sdk.JanusPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class JanusVideoRoomPlugInAPI {
	private JanusRestApiClient janusRestApiClient;
	
	public JanusVideoRoomPlugInAPI( JanusRestApiClient janusRestApiClient ) {
		this.janusRestApiClient = janusRestApiClient;
	}
	
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
	
	public JSONObject createJanusRoom( @NotNull String roomId, @Nullable String description,
	                                   @Nullable String pin, @Nullable String secret, @Nullable String publishers,
	                                   boolean permanent, boolean record, @Nullable String rec_dir ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_VIDEO_ROOM);
		JSONObject json      = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		String response = "";
		// We have a problem here we don't know if Video room is configured to use string room id or integer value only .
		// because of this one of the types will cause an exception if the type use is not accepted by the plugin.
		try {
			json.put("body", new JSONObject()
					.put("request", "create")
					.put("description", description)
					.put("pin", pin)
					.put("secret", secret)
					.put("publishers", publishers)
					.put("permanent", permanent)
					.put("record", record)
					.put("rec_dir", rec_dir)
					.put("room", Integer.parseInt(roomId)/*create assuming integer id would work*/)
			);
			response = janusRestApiClient.makePostRequest(json);
			JSONObject responseObject = new JSONObject(response);
			if (responseObject.getString("janus").equals("success")) {
				JSONObject plugindata = responseObject.getJSONObject("plugindata");
				JSONObject data       = plugindata.getJSONObject("data");
				if (data.has("videoroom") && data.getString("videoroom").equals("created")) {
					return responseObject;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			response = "";
		}
		try {
			json.put("body", new JSONObject()
					.put("request", "create")
					.put("description", description)
					.put("pin", pin)
					.put("secret", secret)
					.put("publishers", publishers)
					.put("permanent", permanent)
					.put("record", record)
					.put("rec_dir", rec_dir)
					.put("room", roomId /*create assuming string id would work*/)
			);
			response = janusRestApiClient.makePostRequest(json);
			JSONObject responseObject = new JSONObject(response);
			if (responseObject.getString("janus").equals("success")) {
				JSONObject plugindata = responseObject.getJSONObject("plugindata");
				JSONObject data       = plugindata.getJSONObject("data");
				if (data.has("videoroom") && data.getString("videoroom").equals("created")) {
					return responseObject;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		return new JSONObject();
	}
	
	public JSONObject checkIfVideoRoomExists( String roomId ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_VIDEO_ROOM);
		
		// We have a problem here we don't know if Video room is configured to use string room id or integer value only .
		// because of this one of the types will cause an exception if the type use is not accepted by the plugin.
		// so we have to make two attempts one with string and/or the other with integer.
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		
		String response;
		try {
			//
			json.put("body",
					new JSONObject()
							.put("request", "exists")
							.put("room", Integer.parseInt(roomId))/* as integer value*/
			);
			response = janusRestApiClient.makePostRequest(json);
			JSONObject responseObject = new JSONObject(response);
			if (responseObject.getString("janus").equals("success")
					&& responseObject.has("plugindata") && responseObject.getJSONObject("plugindata").getJSONObject("data").has("error_code")
			) {
				response = "";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
		
		if (response == null || response.isEmpty()) {
			try {
				//
				json.put("body",
						new JSONObject()
								.put("request", "exists")
								.put("room", roomId)/* as String value*/
				);
				response = janusRestApiClient.makePostRequest(json);
				
			} catch (Exception e) {
				e.printStackTrace();
				return new JSONObject();
			}
		}
		
		return new JSONObject(response);
		
	}
	
}
