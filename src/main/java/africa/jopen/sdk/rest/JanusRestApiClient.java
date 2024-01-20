package africa.jopen.sdk.rest;

import africa.jopen.sdk.JanusPlugins;
import africa.jopen.sdk.SdkUtils;
import africa.jopen.sdk.models.JanusConfiguration;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class JanusRestApiClient {
	static  Logger     log        = Logger.getLogger(JanusRestApiClient.class.getName());
	private HttpClient httpClient = HttpClient.newBuilder().build();
	
	private final JanusConfiguration      janusConfiguration;
	public   final JanusVideoRoomPlugInAPI janusVideoRoomPlugInAPI;
	
	
	public JanusConfiguration getJanusConfiguration() {
		return janusConfiguration;
	}
	
	public JanusRestApiClient( JanusConfiguration janusConfiguration ) {
		this.janusConfiguration = janusConfiguration;
		log.info("JanusRestApiClient initialized");
		janusVideoRoomPlugInAPI = new JanusVideoRoomPlugInAPI(this);
	}
	
	protected String makePostRequest( JSONObject json ) throws Exception {
		json.put("admin_key", janusConfiguration.adminKey());
		json.put("apisecret", janusConfiguration.apiSecret());
		json.put("admin_secret", janusConfiguration.adminSecret());
		json.put("transaction", SdkUtils.uniqueIDGenerator("transaction", 18));
		HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(janusConfiguration.url()))
				.POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
				.header("Content-Type", "application/json")
				.build();
		
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		
		if (response.statusCode() >= 200 && response.statusCode() < 300) {
			return response.body();
		} else {
			throw new Exception("Unexpected response status: " + response.statusCode());
		}
	}
	
	
	protected long attachPlugin( final long sessionId, JanusPlugins plugin ) {
		if (sessionId == 0) {
			log.severe("Failed to attach plugin: session id is 0");
			return 0;
		}
		if (plugin == null) {
			log.severe("Failed to attach plugin: plugin is null");
			return 0;
		}
		JSONObject json = new JSONObject();
		json.put("janus", "attach");
		json.put("session_id", sessionId);
		json.put("plugin", plugin.toString());
		return makeRequestAndHandleResponse(json, "Failed to setup Janus handle");
	}
	
	protected long setupJanusSession() {
		JSONObject json = new JSONObject();
		json.put("janus", "create");
		return makeRequestAndHandleResponse(json, "Failed to setup Janus session");
	}
	
	protected long makeRequestAndHandleResponse( JSONObject json, String errorMessage ) {
		try {
			String     response   = makePostRequest(json);
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.has("janus") && jsonObject.getString("janus").equals("success") && jsonObject.has("data")) {
				return jsonObject.getJSONObject("data").getLong("id");
			} else {
				log.severe(errorMessage + ": " + response);
				return 0;
			}
		} catch (Exception e) {
			log.severe(errorMessage + ": " + e.getMessage());
			return 0;
		}
	}
}
