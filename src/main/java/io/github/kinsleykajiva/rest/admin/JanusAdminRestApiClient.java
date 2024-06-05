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

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JanusAdminRestApiClient {
	
	private static final Logger log = Logger.getLogger(JanusAdminRestApiClient.class.getName());
	private final HttpClient httpClient;
	private final JanusConfiguration janusConfiguration;
	private final JSONObject JANUS_SERVER_ACCESS;
	
	public JanusAdminRestApiClient(JanusConfiguration janusConfiguration) {
		this.janusConfiguration = janusConfiguration;
		this.httpClient = HttpClient.newBuilder().build();
		this.JANUS_SERVER_ACCESS = new JSONObject();
		initializeServerAccess();
		log.info("JanusRestApiClient initialized");
	}
	
	private void initializeServerAccess() {
		JANUS_SERVER_ACCESS.put(Protocol.JANUS.ADMIN_KEY, janusConfiguration.adminKey());
		JANUS_SERVER_ACCESS.put(Protocol.JANUS.ADMIN_SECRET, janusConfiguration.adminSecret());
	}
	
	private CompletableFuture<String> makePostRequestAsync(JSONObject json) {
		JANUS_SERVER_ACCESS.put(Protocol.JANUS.TRANSACTION, SdkUtils.uniqueIDGenerator("transaction", 18));
		JSONObject combined = new JSONObject(JANUS_SERVER_ACCESS, JSONObject.getNames(JANUS_SERVER_ACCESS));
		for (String key : json.keySet()) {
			combined.put(key, json.get(key));
		}
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(janusConfiguration.url() + "/admin"))
				.POST(HttpRequest.BodyPublishers.ofString(combined.toString(), StandardCharsets.UTF_8))
				.header("Content-Type", "application/json")
				.build();
		
		return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(response -> {
					if (response.statusCode() >= 200 && response.statusCode() < 300) {
						return response.body();
					} else {
						throw new RuntimeException("Unexpected response status: " + response.statusCode());
					}
				});
	}
	
	public class ConfigurationRelatedRequests {
		
		public CompletableFuture<List<Long>> getCurrentSession() {
			return makePostRequestAsync(new JSONObject().put("janus", "list_sessions"))
					.thenApply(response -> {
						List<Long> sessionList = new ArrayList<>();
						JSONArray sessions = new JSONObject(response).getJSONArray("sessions");
						for (int i = 0; i < sessions.length(); i++) {
							sessionList.add(sessions.getLong(i));
						}
						return sessionList;
					})
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error fetching current sessions", ex);
						return new ArrayList<>();
					});
		}
		
		public CompletableFuture<List<Long>> getCurrentSessionHandles(long sessionId) {
			return makePostRequestAsync(new JSONObject().put("janus", "list_handles").put("session_id", sessionId))
					.thenApply(response -> {
						List<Long> handleList = new ArrayList<>();
						JSONArray handles = new JSONObject(response).getJSONArray("handles");
						for (int i = 0; i < handles.length(); i++) {
							handleList.add(handles.getLong(i));
						}
						return handleList;
					})
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error fetching session handles", ex);
						return new ArrayList<>();
					});
		}
		
		public CompletableFuture<JSONObject> getHandleInformation(long handleId, long sessionId) {
			return makePostRequestAsync(new JSONObject().put("janus", "handle_info").put("session_id", sessionId).put("handle_id", handleId))
					.thenApply(response -> new JSONObject(response).getJSONObject("info"))
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error fetching handle information", ex);
						return null;
					});
		}
	}
	
	public class TokenRelatedRequests {
		
		public CompletableFuture<List<String>> addToken(@NotNull String token, JanusPlugins[] plugins) {
			JSONObject requestPayload = new JSONObject().put("janus", "add_token").put("token", token);
			if (plugins != null) {
				JSONArray pluginsJsonArray = new JSONArray();
				Arrays.stream(plugins).forEach(plugin -> pluginsJsonArray.put(plugin.toString()));
				requestPayload.put("plugins", pluginsJsonArray);
			}
			
			return makePostRequestAsync(requestPayload)
					.thenApply(response -> {
						List<String> pluginList = new ArrayList<>();
						JSONObject responseJson = new JSONObject(response);
						if (responseJson.getString("janus").equals("error")) {
							log.warning("Janus Error: " + responseJson.getJSONObject("error").getString("reason"));
							return pluginList;
						}
						JSONArray pluginsArray = responseJson.getJSONObject("data").getJSONArray("plugins");
						for (int i = 0; i < pluginsArray.length(); i++) {
							pluginList.add(pluginsArray.getString(i));
						}
						return pluginList;
					})
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error adding token", ex);
						return new ArrayList<>();
					});
		}
		
		public CompletableFuture<List<TokenRecord>> listTokens() {
			JSONObject requestPayload = new JSONObject().put("janus", "list_tokens");
			return makePostRequestAsync(requestPayload)
					.thenApply(response -> {
						List<TokenRecord> tokensList = new ArrayList<>();
						JSONArray tokensArray = new JSONObject(response).getJSONObject("data").getJSONArray("tokens");
						for (int i = 0; i < tokensArray.length(); i++) {
							JSONObject tokenObj = tokensArray.getJSONObject(i);
							String token = tokenObj.getString("token");
							JSONArray allowedPluginsArray = tokenObj.getJSONArray("allowed_plugins");
							JanusPlugins[] plugins = new JanusPlugins[allowedPluginsArray.length()];
							for (int j = 0; j < allowedPluginsArray.length(); j++) {
								plugins[j] = JanusPlugins.valueOf(allowedPluginsArray.getString(j));
							}
							tokensList.add(new TokenRecord(token, plugins));
						}
						return tokensList;
					})
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error listing tokens", ex);
						return new ArrayList<>();
					});
		}
		
		public CompletableFuture<Boolean> removeToken(String token) {
			JSONObject requestPayload = new JSONObject().put("janus", "remove_token").put("token", token);
			return makePostRequestAsync(requestPayload)
					.thenApply(response -> new JSONObject(response).getString("janus").equals("success"))
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error removing token", ex);
						return false;
					});
		}
		
		public CompletableFuture<Boolean> allowToken(@NotNull String token, @NotNull JanusPlugins[] plugins) {
			JSONArray pluginsJsonArray = new JSONArray();
			Arrays.stream(plugins).forEach(plugin -> pluginsJsonArray.put(plugin.toString()));
			JSONObject requestPayload = new JSONObject().put("janus", "allow_token").put("token", token).put("plugins", pluginsJsonArray);
			
			return makePostRequestAsync(requestPayload)
					.thenApply(response -> new JSONObject(response).getString("janus").equals("success"))
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error allowing token", ex);
						return false;
					});
		}
		
		public CompletableFuture<Boolean> disallowToken(@NotNull String token, @NotNull JanusPlugins[] plugins) {
			JSONArray pluginsJsonArray = new JSONArray();
			Arrays.stream(plugins).forEach(plugin -> pluginsJsonArray.put(plugin.toString()));
			JSONObject requestPayload = new JSONObject().put("janus", "disallow_token").put("token", token).put("plugins", pluginsJsonArray);
			
			return makePostRequestAsync(requestPayload)
					.thenApply(response -> new JSONObject(response).getString("janus").equals("success"))
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error disallowing token", ex);
						return false;
					});
		}
	}
	
	public  class SessionRelatedRequests {
		public CompletableFuture<Boolean> acceptOrReject(Boolean accept) {
			JSONObject requestPayload = new JSONObject().put("janus", "accept_new_sessions").put("accept", accept);
			return makePostRequestAsync(requestPayload)
					.thenApply(response -> new JSONObject(response).getString("janus").equals("success"))
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error removing token", ex);
						return false;
					});
		}
		
		
		public CompletableFuture<Boolean> setTimeOut(int timeout) {
			JSONObject requestPayload = new JSONObject().put("janus", "set_session_timeout").put("timeout", timeout);
			return makePostRequestAsync(requestPayload)
					.thenApply(response -> new JSONObject(response).getString("janus").equals("success"))
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error removing token", ex);
						return false;
					});
		}
		
		public CompletableFuture<Boolean> destroySession(long session) {
			JSONObject requestPayload = new JSONObject().put("janus", "destroy_session").put("session", session);
			return makePostRequestAsync(requestPayload)
					.thenApply(response -> new JSONObject(response).getString("janus").equals("success"))
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error removing token", ex);
						return false;
					});
		}
		
		
		public CompletableFuture<List<Long>> getCurrentSession() {
			return makePostRequestAsync(new JSONObject().put("janus", "list_sessions"))
					.thenApply(response -> {
						List<Long> sessionList = new ArrayList<>();
						JSONArray  sessions    = new JSONObject(response).getJSONArray("sessions");
						for (int i = 0; i < sessions.length(); i++) {
							sessionList.add(sessions.getLong(i));
						}
						return sessionList;
					})
					.exceptionally(ex -> {
						log.log(Level.SEVERE, "Error fetching current sessions", ex);
						return new ArrayList<>();
					});
		}
	}
	
	public  class HandleWebRTCRelatedRequests {
		// To be implemented
	}
	
	public  class TransportRelatedRequests {
		// To be implemented
	}
	
	public  class EventHandlersRelatedRequests {
		// To be implemented
	}
	
	public  class HelperRelatedRequests {
		// To be implemented
	}
}
