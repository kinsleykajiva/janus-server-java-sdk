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
	
	public JSONObject createJanusRoom(@NotNull String roomId, @Nullable String description,
	                                  @Nullable String pin, @Nullable String secret, int publishers,
	                                  boolean permanent, boolean record, @Nullable String rec_dir) {
		if (checkIfVideoRoomExistsBoolCheck(roomId)) {
			return null;
		}
		
		if (publishers < 1) {
			publishers = 1;
		}
		
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_VIDEO_ROOM);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		
		try {
			// We have a problem here we don't know if Video room is configured to use string room id or integer value only .
			
			for (String idType : new String[]{"integer", "string"}) {
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
				System.out.println("xxxx=> " + response);
				JSONObject responseObject = new JSONObject(response);
				
				if (responseObject.getString("janus").equals("success")) {
					JSONObject plugindata = responseObject.getJSONObject("plugindata");
					JSONObject data = plugindata.getJSONObject("data");
					if (data.has("videoroom") && data.getString("videoroom").equals("created")) {
						return responseObject;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new JSONObject();
	}
	public JSONObject checkIfVideoRoomExists(String roomId) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId = janusRestApiClient.attachPlugin(sessionId, JanusPlugins.JANUS_VIDEO_ROOM);
		
		JSONObject json = new JSONObject();
		json.put("janus", "message");
		json.put("handle_id", handleId);
		json.put("session_id", sessionId);
		
		// We have a problem here we don't know if Video room is configured to use string room id or integer value only .
		// because of this one of the types will cause an exception if the type use is not accepted by the plugin.
		// so we have to make two attempts one with string and/or the other with integer.
		
		for (String idType : new String[]{"integer", "string"}) {
			try {
				json.put("body",
						new JSONObject()
								.put("request", "exists")
								.put("room", idType.equals("integer") ? Integer.parseInt(roomId) : roomId)
				);
				
				String response = janusRestApiClient.makePostRequest(json);
				JSONObject responseObject = new JSONObject(response);
				
				if (responseObject.getString("janus").equals("success")
						&& responseObject.has("plugindata") && responseObject.getJSONObject("plugindata").getJSONObject("data").has("error_code")) {
					return new JSONObject();
				}
				return  responseObject;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return new JSONObject();
	}
	
}
