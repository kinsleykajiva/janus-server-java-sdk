package io.github.kinsleykajiva;


import io.github.kinsleykajiva.cache.DBAccess;
import io.github.kinsleykajiva.models.JanusConfiguration;
import io.github.kinsleykajiva.models.JanusSession;
import io.github.kinsleykajiva.cache.mysql.MySqlConfiguration;
import io.github.kinsleykajiva.net.JanusWebSocketClient;
import io.github.kinsleykajiva.rest.JanusRestApiClient;
import io.github.kinsleykajiva.utils.JanusEventHandler;
import io.github.kinsleykajiva.utils.SdkUtils;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The Janus class represents a Janus instance that communicates with the Janus server.
 */
public class Janus implements JanusEventHandler {
	static        Logger                   log                      = Logger.getLogger(Janus.class.getName());
	private final ScheduledExecutorService   keppAliveExecutorService = new ScheduledThreadPoolExecutor(1);
	private final CopyOnWriteArrayList<Long> PluginHandles            = new CopyOnWriteArrayList<>();
	
	public static DBAccess DB_ACCESS = null;
	private       String   sessionTransactionId;
	
	private JanusWebSocketClient webSocketClient;
	private JanusSession         janusSession;
	private boolean              isAPIAccessOnly = false;
	private JanusConfiguration janusConfiguration;
	public  JanusRestApiClient janusRestApiClient;
	
	
	
	/**
	 * Constructs a Janus instance based on the provided configuration.
	 *
	 * @param isAPIAccessOnly Flag indicating whether Janus is running in API Access Only mode.
	 *                        If true, Janus will use REST API for communication. If false, Janus will use WebSocket.
	 * @param config          The JanusConfiguration object containing the server connection details.
	 *                        It should include the URL, API secret, admin key, and admin secret.
	 * @throws IllegalArgumentException If the provided configuration object is null.
	 */
	public Janus( boolean isAPIAccessOnly, @NotNull JanusConfiguration config ) {
		if (isAPIAccessOnly) {
			this.isAPIAccessOnly = true;
			log.info("Janus is running in API Access Only mode" );
			janusConfiguration = new JanusConfiguration( SdkUtils.convertFromWebSocketUrl( config.url()),config.apiSecret(), config.adminKey(), config.adminSecret());
			janusRestApiClient = new JanusRestApiClient(janusConfiguration);
		} else {
			String finalUrl = SdkUtils.convertToWebSocketUrl(janusConfiguration.url());
			janusConfiguration = new JanusConfiguration(config.url(),config.apiSecret(), config.adminKey(), config.adminSecret());
			try {
				webSocketClient = new JanusWebSocketClient(finalUrl, this);
			} catch (Exception e) {
				log.severe("Failed to initialize JanusWebSocketClient: " + e.getMessage());
			}
			SdkUtils.runAfter(5, () -> {
				webSocketClient.initializeWebSocket();
			});
		}
	}
	
	
	@NonBlocking
	private void keepAlive() {
		
		keppAliveExecutorService.scheduleAtFixedRate(() -> {
			JSONObject message = new JSONObject();
			message.put("janus", "keepalive");
			message.put("session_id", janusSession.id());
			sendMessage(message);
		}, 0, 25, TimeUnit.SECONDS);
	}
	
	@NonBlocking
	private void createSession() {
		JSONObject message = new JSONObject();
		sessionTransactionId = SdkUtils.uniqueIDGenerator("transaction", 18);
		message.put("transaction", sessionTransactionId);
		message.put("janus", "create");
		sendMessage(message);
	}
	
	public void sendMessage( final JSONObject message ) {
		if (!message.has("transaction")) {
			message.put("transaction", SdkUtils.uniqueIDGenerator("transaction", 18));
		}
		log.info("Sending message: " + message);
		webSocketClient.send(message.toString());
	}
	
	@NonBlocking
	private void getInfo() {
	
	}
	
	@NonBlocking
	private void destroySession() {
	
	}
	
	@NonBlocking
	private void createDataChannel() {
	
	}
	
	@NonBlocking
	private void destroyHandle() {
	
	}
	
	@NonBlocking
	private void attachePlugin() {
		JSONObject plugin = new JSONObject();
		sessionTransactionId = SdkUtils.uniqueIDGenerator("transaction", 18);
		plugin.put("transaction", sessionTransactionId);
		plugin.put("janus", "attach");
		plugin.put("session_id", janusSession.id());
		plugin.put("plugin", "janus.plugin.videoroom");
		sendMessage(plugin);
		
	}
	
	@NonBlocking
	private void sendDtmf() {
	
	}
	
	
	@Override
	@NonBlocking
	public void handleEvent( @NotNull JSONObject event ) {
		if (event.has("janus")) {
			String janus = event.getString("janus");
			switch (janus) {
				case "success" -> {
					var transaction = event.getString("transaction");
					if (transaction.equals(sessionTransactionId)) {
						var data = event.getJSONObject("data");
						if (janusSession == null) {
							var sessionId = data.getLong("id");
							log.info("Session created: " + sessionId);
							janusSession = new JanusSession(sessionId);
							sessionTransactionId = "";
							keepAlive();
							
							SdkUtils.runAfter(8, this::attachePlugin);
						} else {
							// must be a plugin attachment act
							var handleId = data.getLong("id");
							PluginHandles.add(handleId);
							log.info("Plugin handleId attached: " + handleId);
							sessionTransactionId = "";
						}
						
					}
				}
				case "keepalive" -> {
				}
				case "event" -> {
				}
				case "ack" -> {
				}
				case "hangup" -> {
				}
				case "detached" -> {
				}
				case "webrtcup" -> {
				}
				
				case "trickle" -> {
				}
				case "media" -> {
				}
				case "slowlink" -> {
				}
				case "error" -> {
				}
				case "timeout" -> {
				}
				default -> {
				}
			}
		}
	}
	
	@Override
	public void onConnected() {
		
		log.info("Connected to Janus");
		createSession();
	}
	
	
}
