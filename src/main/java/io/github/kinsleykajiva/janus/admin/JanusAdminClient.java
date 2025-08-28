package io.github.kinsleykajiva.janus.admin;

import io.github.kinsleykajiva.janus.utils.JanusUtils;
import io.github.kinsleykajiva.janus.utils.ServerInfo;
import io.github.kinsleykajiva.janus.admin.messages.AcceptNewSessions;
import io.github.kinsleykajiva.janus.admin.messages.CustomEvent;
import io.github.kinsleykajiva.janus.admin.messages.DestroySession;
import io.github.kinsleykajiva.janus.admin.messages.DetachHandle;
import io.github.kinsleykajiva.janus.admin.messages.GetStatus;
import io.github.kinsleykajiva.janus.admin.messages.HandleInfo;
import io.github.kinsleykajiva.janus.admin.messages.HandleInfoResponse;
import io.github.kinsleykajiva.janus.admin.messages.HangupWebRTC;
import io.github.kinsleykajiva.janus.admin.messages.Info;
import io.github.kinsleykajiva.janus.admin.messages.ListHandles;
import io.github.kinsleykajiva.janus.admin.messages.ListHandlesResponse;
import io.github.kinsleykajiva.janus.admin.messages.ListSessions;
import io.github.kinsleykajiva.janus.admin.messages.ListSessionsResponse;
import io.github.kinsleykajiva.janus.admin.messages.MessagePlugin;
import io.github.kinsleykajiva.janus.admin.messages.Ping;
import io.github.kinsleykajiva.janus.admin.messages.QueryEventHandler;
import io.github.kinsleykajiva.janus.admin.messages.ResolveAddress;
import io.github.kinsleykajiva.janus.admin.messages.SetLogLevel;
import io.github.kinsleykajiva.janus.admin.messages.SetSessionTimeout;
import io.github.kinsleykajiva.janus.admin.messages.StartPcap;
import io.github.kinsleykajiva.janus.admin.messages.StartText2Pcap;
import io.github.kinsleykajiva.janus.admin.messages.StopPcap;
import io.github.kinsleykajiva.janus.admin.messages.StopText2Pcap;
import io.github.kinsleykajiva.janus.admin.messages.TestStun;
import io.github.kinsleykajiva.janus.utils.JanusException;
import io.github.kinsleykajiva.janus.utils.TransactionManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.*;

import static io.github.kinsleykajiva.janus.utils.JanusUtils.convertToServerInfo;

public class JanusAdminClient implements WebSocket.Listener {
    private static final Logger logger = LoggerFactory.getLogger(JanusAdminClient.class);
    private static final long DEFAULT_CONNECTION_TIMEOUT_MS = 10_000; // 10 seconds

    private final JanusAdminConfiguration config;
    private final HttpClient httpClient;
    private WebSocket webSocket;
    private final ExecutorService executor;
    private final TransactionManager transactionManager;
    private final StringBuilder messageBuffer = new StringBuilder();
    private final JanusAdminMonitor adminMonitor;

    public JanusAdminClient(JanusAdminConfiguration config) {
        this.config = config;
        this.transactionManager = new TransactionManager();
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.httpClient = HttpClient.newBuilder().executor(this.executor).build();
        this.adminMonitor = new JanusAdminMonitor();

        try {
            logger.info("Starting admin connection attempt...");
            connect().get();
            logger.info("Admin connection established.");
        } catch (InterruptedException e) {
            logger.info("Program interrupted, shutting down.");
            Thread.currentThread().interrupt(); // Restore interrupted status
        } catch (Exception e) {
            logger.error("Failed to connect to admin interface: {}", e.getMessage(), e);
        }
    }

    public CompletableFuture<Void> connect() {
        logger.info("Connecting to Janus Admin Gateway at {}", config.uri());
        CompletableFuture<Void> connectionFuture = httpClient.newWebSocketBuilder()
                .subprotocols("janus-admin-protocol")
                .buildAsync(config.uri(), this)
                .thenAccept(ws -> this.webSocket = ws);

        return connectionFuture.orTimeout(DEFAULT_CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    logger.error("Failed to connect to Janus Admin Gateway at {}: {}", config.uri(), throwable.getMessage(), throwable);
                    throw new JanusException("Admin connection failed", throwable);
                });
    }

    public void disconnect() {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client requested disconnect").get(5, TimeUnit.SECONDS);
                logger.info("Disconnected from Janus Admin Gateway at {}", config.uri());
            } catch (Exception e) {
                logger.warn("Error during graceful WebSocket disconnect: {}", e.getMessage());
            }
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        logger.info("Successfully connected to Janus Admin Gateway at {}", config.uri());
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        messageBuffer.append(data);
        if (last) {
            String completeMessage = messageBuffer.toString();
            executor.submit(() -> processMessage(completeMessage));
            messageBuffer.setLength(0);
        }
        webSocket.request(1);
        return CompletableFuture.completedFuture(null);
    }

    private void processMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String transactionId = json.optString("transaction", null);
            if (transactionId != null && !transactionId.isEmpty()) {
                transactionManager.completeTransaction(transactionId, json);
            } else {
                adminMonitor.dispatchEvent(json);
            }
        } catch (JSONException e) {
            logger.error("Error parsing JSON message: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error processing message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        logger.error("WebSocket error at {}: {}", config.uri(), error.getMessage(), error);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        logger.warn("WebSocket closed for {}: {} - {}", config.uri(), statusCode, reason);
        return CompletableFuture.completedFuture(null);
    }

    public void sendMessage(JSONObject message) {
        if (webSocket == null || webSocket.isOutputClosed()) {
            logger.error("Cannot send message: WebSocket is not connected to {}", config.uri());
            throw new IllegalStateException("WebSocket is not connected.");
        }
        if (!message.has("admin_secret")) {
            message.put("admin_secret", config.adminSecret());
        }
        String msgStr = message.toString();
        logger.debug("Sending admin message: {}", msgStr);
        webSocket.sendText(msgStr, true);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public JanusAdminMonitor getAdminMonitor() {
        return adminMonitor;
    }

    public CompletableFuture<ListSessionsResponse> listSessions() {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        ListSessions request = new ListSessions(transactionId);
        sendMessage(request.toJson());
        return future.thenApply(ListSessionsResponse::new);
    }

    public CompletableFuture<HandleInfoResponse> handleInfo(long sessionId, long handleId) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        HandleInfo request = new HandleInfo(transactionId, sessionId, handleId);
        sendMessage(request.toJson());
        return future.thenApply(HandleInfoResponse::new);
    }

    public CompletableFuture<ServerInfo> info() {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        Info request = new Info(transactionId);
        sendMessage(request.toJson());
        return future.thenApply(JanusUtils::convertToServerInfo);
    }

   

    public CompletableFuture<JSONObject> ping() {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        Ping request = new Ping(transactionId);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> destroySession(long sessionId) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        DestroySession request = new DestroySession(transactionId, sessionId);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<ListHandlesResponse> listHandles(long sessionId) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        ListHandles request = new ListHandles(transactionId, sessionId);
        sendMessage(request.toJson());
        return future.thenApply(ListHandlesResponse::new);
    }

    public CompletableFuture<JSONObject> messagePlugin(long sessionId, long handleId, JSONObject body) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        MessagePlugin request = new MessagePlugin(transactionId, sessionId, handleId, body);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> getStatus() {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        GetStatus request = new GetStatus(transactionId);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> setLogLevel(int level) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        SetLogLevel request = new SetLogLevel(transactionId, level);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> resolveAddress(String address) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        ResolveAddress request = new ResolveAddress(transactionId, address);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> testStun() {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        TestStun request = new TestStun(transactionId);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> queryEventHandler(String handler, JSONObject query) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        QueryEventHandler request = new QueryEventHandler(transactionId, handler, query);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> customEvent(JSONObject event) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        CustomEvent request = new CustomEvent(transactionId, event);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> startPcap(long sessionId, long handleId, String folder, String filename) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        StartPcap request = new StartPcap(transactionId, sessionId, handleId, folder, filename);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> stopPcap(long sessionId, long handleId) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        StopPcap request = new StopPcap(transactionId, sessionId, handleId);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> startText2Pcap(long sessionId, long handleId, String folder, String filename) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        StartText2Pcap request = new StartText2Pcap(transactionId, sessionId, handleId, folder, filename);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> stopText2Pcap(long sessionId, long handleId) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        StopText2Pcap request = new StopText2Pcap(transactionId, sessionId, handleId);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> hangupWebRTC(long sessionId, long handleId) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        HangupWebRTC request = new HangupWebRTC(transactionId, sessionId, handleId);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> detachHandle(long sessionId, long handleId) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        DetachHandle request = new DetachHandle(transactionId, sessionId, handleId);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> setSessionTimeout(int timeout) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        SetSessionTimeout request = new SetSessionTimeout(transactionId, timeout);
        sendMessage(request.toJson());
        return future;
    }

    public CompletableFuture<JSONObject> acceptNewSessions(boolean accept) {
        String transactionId = transactionManager.createTransaction();
        var future = transactionManager.registerTransaction(transactionId);
        AcceptNewSessions request = new AcceptNewSessions(transactionId, accept);
        sendMessage(request.toJson());
        return future;
    }
}
