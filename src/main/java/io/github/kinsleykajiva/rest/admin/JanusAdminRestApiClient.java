package io.github.kinsleykajiva.rest.admin;

import io.github.kinsleykajiva.models.JanusConfiguration;
import io.github.kinsleykajiva.utils.Protocol;
import io.github.kinsleykajiva.utils.SdkUtils;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JanusAdminRestApiClient {
	
	private final Logger             log                = Logger.getLogger(JanusAdminRestApiClient.class.getName());
	private final HttpClient         httpClient         = HttpClient.newBuilder().build();
	private       JanusConfiguration janusConfiguration = null;
	JSONObject JANUS_SERVER_ACCESS = new JSONObject();
	
	public JanusAdminRestApiClient( JanusConfiguration janusConfiguration ) {
		this.janusConfiguration = janusConfiguration;
		log.info("JanusRestApiClient initialized");
		
		JANUS_SERVER_ACCESS.put(Protocol.JANUS.ADMIN_KEY, janusConfiguration.adminKey());
		JANUS_SERVER_ACCESS.put(Protocol.JANUS.ADMIN_SECRET, janusConfiguration.adminSecret());
		
		
	}
	
	private String makePostRequest( JSONObject json ) throws Exception {
		
		JANUS_SERVER_ACCESS.put(Protocol.JANUS.TRANSACTION, SdkUtils.uniqueIDGenerator("transaction", 18));
		JSONObject combined = new JSONObject();
		for (String key : JANUS_SERVER_ACCESS.keySet()) {
			combined.put(key, JANUS_SERVER_ACCESS.get(key));
		}
		
		for (String key : json.keySet()) {
			combined.put(key, json.get(key));
		}
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(janusConfiguration.url() + "/admin"))
				.POST(HttpRequest.BodyPublishers.ofString(combined.toString(), StandardCharsets.UTF_8))
				.header("Content-Type", "application/json")
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		
		if (response.statusCode() >= 200 && response.statusCode() < 300) {
			return response.body();
		} else {
			throw new Exception("Unexpected response status: " + response.statusCode());
		}
	}
	
	public class ConfigurationRelatedRequests {
		public List<Long> getCurrentSession() {
			List<Long> ret = new ArrayList<>();
			try {
				var response = makePostRequest(
						new JSONObject()
								.put("janus", "list_sessions")
				);
				var responseJ = new JSONObject(response);
				var sessions  = responseJ.getJSONArray("sessions");
				for (int i = 0; i < sessions.length(); i++) {
					long sess = sessions.getLong(i);
					ret.add(sess);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		public List<Long> getCurrentSessionHandles( long session_id ) {
			List<Long> ret = new ArrayList<>();
			try {
				var response = makePostRequest(
						new JSONObject()
								.put("janus", "list_handles")
								.put("session_id", session_id)
				);
				var responseJ = new JSONObject(response);
				var handles   = responseJ.getJSONArray("handles");
				for (int i = 0; i < handles.length(); i++) {
					long handle = handles.getLong(i);
					ret.add(handle);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		public JSONObject getHandleInformation( final Long handle_id, final Long session_id ) {
			List<Long> ret = new ArrayList<>();
			try {
				var response = makePostRequest(
						new JSONObject()
								.put("janus", "handle_info")
								.put("session_id", session_id)
								.put("handle_id", handle_id)
				);
				var responseJ = new JSONObject(response).getJSONObject("info");
				return responseJ;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static class TokenRelatedRequests {
	
	}
	
	public static class SessionRelatedRequests {
	
	}
	
	public static class HandleWebRTCRelatedRequests {
	
	}
	
	public static class TransportRelatedRequests {
	
	}
	
	public static class EventHandlersRelatedRequests {
	
	}
	
	public static class HelperRelatedRequests {
	
	}
	
	
}
