package io.github.kinsleykajiva.rest.admin;

import io.github.kinsleykajiva.models.JanusConfiguration;
import io.github.kinsleykajiva.utils.JanusPlugins;
import io.github.kinsleykajiva.utils.Protocol;
import io.github.kinsleykajiva.utils.SdkUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
	
	public class TokenRelatedRequests {
		public List<String> addToken( @NotNull final String token, JanusPlugins[] plugins ) {
			List<String> ret = new ArrayList<>();
			try {
				String response;
				if (Objects.isNull(plugins)) {
					response = makePostRequest(
							new JSONObject()
									.put("janus", "add_token")
									.put("token", token)
					);
				} else {
					var pluginsJsonArray = new JSONArray();
					Arrays.asList(plugins).forEach(p -> pluginsJsonArray.put(p.toString()));
					response = makePostRequest(
							new JSONObject()
									.put("janus", "add_token")
									.put("token", token)
									.put("plugins", pluginsJsonArray)
					);
				}
				
				if (new JSONObject(response).getString("janus").equals("error")) {
					log.info("Janus Error, " + new JSONObject(response).getJSONObject("error").getString("reason"));
					return ret;
				}
				var responseJ = new JSONObject(response).getJSONObject("data");
				var plugins_  = responseJ.getJSONArray("plugins");
				for (int i = 0; i < plugins_.length(); i++) {
					var p = plugins_.getString(i);
					ret.add(p);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		public List<TokenRecord> listTokens() {
			List<TokenRecord> tokensList = new ArrayList<>();
			
			try {
				JSONObject requestPayload = new JSONObject().put("janus", "list_tokens");
				String     response       = makePostRequest(requestPayload);
				
				JSONObject responseJson = new JSONObject(response).getJSONObject("data");
				JSONArray  tokensArray  = responseJson.getJSONArray("tokens");
				
				for (int i = 0; i < tokensArray.length(); i++) {
					JSONObject tokenObj = tokensArray.getJSONObject(i);
					String     token    = tokenObj.getString("token");
					
					JSONArray      allowedPluginsArray = tokenObj.getJSONArray("allowed_plugins");
					JanusPlugins[] plugins             = new JanusPlugins[allowedPluginsArray.length()];
					
					for (int j = 0; j < allowedPluginsArray.length(); j++) {
						String pluginName = allowedPluginsArray.getString(j);
						plugins[j] = JanusPlugins.valueOf(pluginName);
					}
					
					tokensList.add(new TokenRecord(token, plugins));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return tokensList;
		}
		
		public Boolean removeToken( String token ) {
			
			try {
				var response = makePostRequest(
						new JSONObject()
								.put("janus", "remove_token")
								.put("token", token)
				
				);
				return new JSONObject(response).getString("janus").equals("success");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Boolean.FALSE;
		}
		
		public Boolean allowToken( @NotNull String token, @NotNull JanusPlugins[] plugins ) {
			
			try {
				var pluginsJsonArray = new JSONArray();
				Arrays.asList(plugins).forEach(p -> pluginsJsonArray.put(p.toString()));
				var response = makePostRequest(
						new JSONObject()
								.put("janus", "allow_token")
								.put("token", token)
								.put("plugins", pluginsJsonArray)
				
				);
				return new JSONObject(response).getString("janus").equals("success");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Boolean.FALSE;
		}
		public Boolean disallowToken( @NotNull String token, @NotNull JanusPlugins[] plugins ) {
			
			try {
				var pluginsJsonArray = new JSONArray();
				Arrays.asList(plugins).forEach(p -> pluginsJsonArray.put(p.toString()));
				var response = makePostRequest(
						new JSONObject()
								.put("janus", "disallow_token")
								.put("token", token)
								.put("plugins", pluginsJsonArray)
				
				);
				return new JSONObject(response).getString("janus").equals("success");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Boolean.FALSE;
		}
		
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
