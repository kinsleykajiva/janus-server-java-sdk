package io.github.kinsleykajiva.janus.admin;

import io.github.kinsleykajiva.janus.admin.messages.HandleInfo;
import io.github.kinsleykajiva.janus.admin.messages.HandleInfoResponse;
import io.github.kinsleykajiva.janus.admin.messages.ListSessions;
import io.github.kinsleykajiva.janus.admin.messages.ListSessionsResponse;
import io.github.kinsleykajiva.janus.exception.JanusException;
import io.github.kinsleykajiva.janus.internal.TransactionManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.*;

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
}
