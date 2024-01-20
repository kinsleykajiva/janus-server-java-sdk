package africa.jopen.sdk.rest;

import africa.jopen.sdk.JanusPlugins;
import org.json.JSONObject;

public class JanusVideoRoomPlugInAPI {
	private JanusRestApiClient janusRestApiClient;
	
	public JanusVideoRoomPlugInAPI( JanusRestApiClient janusRestApiClient ) {
		this.janusRestApiClient = janusRestApiClient;
	}
	
	public  JSONObject checkIfVideoRoomExists( JanusPlugins plugin, String roomId ) {
		final long sessionId = janusRestApiClient.setupJanusSession();
		final long handleId  = janusRestApiClient.attachPlugin(sessionId, plugin);
		
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
