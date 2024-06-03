package io.github.kinsleykajiva;

import io.github.kinsleykajiva.cache.DBAccess;
import io.github.kinsleykajiva.models.JanusConfiguration;
import io.github.kinsleykajiva.models.JanusSession;
import io.github.kinsleykajiva.net.JanusWebSocketClient;
import io.github.kinsleykajiva.rest.JanusRestApiClient;
import io.github.kinsleykajiva.utils.JanusEventHandler;
import io.github.kinsleykajiva.utils.JanusPlugins;
import io.github.kinsleykajiva.utils.Protocol;
import io.github.kinsleykajiva.utils.SdkUtils;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

/** The Janus class represents a Janus instance that communicates with the Janus server. */
public class Janus implements JanusEventHandler {
  private final Logger                            log               = Logger.getLogger(Janus.class.getName());
  private final ScheduledExecutorService   keepAliveExecutorService = new ScheduledThreadPoolExecutor(1);
  private final CopyOnWriteArrayList<Long> PluginHandles            = new CopyOnWriteArrayList<>();
  
  public static DBAccess             DB_ACCESS            = null;
  private       String               sessionTransactionId = null;
  private       JanusWebSocketClient webSocketClient      = null;
  private       JanusSession         janusSession         = null;
  private       boolean              isAPIAccessOnly      = false;
  private       JanusConfiguration   janusConfiguration   = null;
  public        JanusRestApiClient   janusRestApiClient   = null;

  /**
   * Constructs a Janus instance based on the provided configuration.
   *
   * @param isAPIAccessOnly Flag indicating whether Janus is running in API Access Only mode. If
   *     true, Janus will use REST API for communication. If false, Janus will use WebSocket.
   * @param config The JanusConfiguration object containing the server connection details. It should
   *     include the URL, API secret, admin key, and admin secret.
   * @throws IllegalArgumentException If the provided configuration object is null.
   */
  public Janus( boolean isAPIAccessOnly, @NotNull JanusConfiguration config ) {
    if (isAPIAccessOnly) {
      this.isAPIAccessOnly = true;
      log.info("Janus is running in API Access Only mode");
      janusConfiguration =
              new JanusConfiguration(
                      SdkUtils.convertFromWebSocketUrl(config.url()),
                      config.apiSecret(),
                      config.adminKey(),
                      config.adminSecret());
      janusRestApiClient = new JanusRestApiClient(janusConfiguration);
    } else {
      String finalUrl    = SdkUtils.convertToWebSocketUrl(janusConfiguration.url());
      janusConfiguration = new JanusConfiguration(config.url()                      , config.apiSecret(), config.adminKey(), config.adminSecret());
      try {
        webSocketClient = new JanusWebSocketClient(finalUrl, this);
      } catch (Exception e) {
        log.severe("Failed to initialize Janus Web Socket Client: " + e.getMessage());
      }
      SdkUtils.runAfter(5, () -> webSocketClient.initializeWebSocket());
    }
  }

  @NonBlocking
  private void keepAlive() {

    keepAliveExecutorService.scheduleAtFixedRate(
        () -> {
          JSONObject message = new JSONObject();
          message.put( Protocol.JANUS.JANUS, Protocol.JANUS.REQUEST.KEEPALIVE);
          message.put(Protocol.JANUS.SESSION_ID, janusSession.id());
          sendMessage(message);
        },
        0,
        25,
        TimeUnit.SECONDS);
  }

  @NonBlocking
  private void createSession() {
    JSONObject message   = new JSONObject();
    sessionTransactionId = SdkUtils.uniqueIDGenerator(Protocol.JANUS.TRANSACTION, 18);
    message.put(Protocol.JANUS.TRANSACTION, sessionTransactionId);
    message.put( Protocol.JANUS.JANUS,Protocol.JANUS.REQUEST.CREATE_SESSION);
    sendMessage(message);
  }

  public void sendMessage(final JSONObject message) {
    if (!message.has(Protocol.JANUS.TRANSACTION)) {
      message.put(Protocol.JANUS.TRANSACTION, SdkUtils.uniqueIDGenerator(Protocol.JANUS.TRANSACTION, 18));
    }
    log.info("Sending message: " + message);
    webSocketClient.send(message.toString());
  }

  @NonBlocking
  private void getInfo() {}

  @NonBlocking
  private void destroySession() {}

  @NonBlocking
  private void createDataChannel() {}

  @NonBlocking
  private void destroyHandle() {}

  @NonBlocking
  private void attachePlugin() {
    JSONObject plugin = new JSONObject();
    sessionTransactionId = SdkUtils.uniqueIDGenerator(Protocol.JANUS.TRANSACTION, 18);
    plugin.put( Protocol.JANUS.TRANSACTION, sessionTransactionId);
    plugin.put(  Protocol.JANUS.JANUS,  Protocol.JANUS.REQUEST.ATTACH_PLUGIN);
    plugin.put(  Protocol.JANUS.SESSION_ID, janusSession.id());
    plugin.put( Protocol.JANUS.PLUG_IN, JanusPlugins.JANUS_VIDEO_ROOM);
    sendMessage(plugin);
  }

  @NonBlocking
  private void sendDtmf() {}

  @Override
  @NonBlocking
  public void handleEvent(@NotNull JSONObject event) {
    if (event.has( Protocol.JANUS.JANUS)) {
      String janus = event.getString( Protocol.JANUS.JANUS);
      switch (janus) {
	      case Protocol.JANUS.RESPONSE.SUCCESS -> {
          var transaction = event.getString(Protocol.JANUS.TRANSACTION);
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
        case Protocol.JANUS.REQUEST.KEEPALIVE -> {}
        case Protocol.JANUS.EVENT.EVENT       ->{}
        case Protocol.JANUS.ACK               -> {}
        case Protocol.JANUS.EVENT.HANGUP      -> {}
        case Protocol.JANUS.EVENT.DETACHED    -> {}
        case Protocol.JANUS.EVENT.WEBRTCUP    -> {}
        case Protocol.JANUS.EVENT.TRICKLE     -> {}
        case Protocol.JANUS.EVENT.MEDIA       -> {}
        case Protocol.JANUS.EVENT.SLOWLINK    -> {}
        case Protocol.JANUS.RESPONSE.ERROR    -> {}
        case Protocol.JANUS.EVENT.TIMEOUT     -> {}
        default                               -> {}
      }
    }
  }

  @Override
  public void onConnected() {

    log.info("Connected to Janus");
    createSession();
  }
}
