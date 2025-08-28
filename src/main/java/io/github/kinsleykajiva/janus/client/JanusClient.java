package io.github.kinsleykajiva.janus.client;

import io.github.kinsleykajiva.janus.utils.JanusException;
import io.github.kinsleykajiva.janus.utils.JanusUtils;
import io.github.kinsleykajiva.janus.utils.ServerInfo;
import io.github.kinsleykajiva.janus.utils.TransactionManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class JanusClient implements WebSocket.Listener {
	private static final Logger logger                           = LoggerFactory.getLogger(JanusClient.class);
	private static final long DEFAULT_CONNECTION_TIMEOUT_MS      = 10_000;                                                                  // 10 seconds
	private static final long SERVER_INFO_TIMEOUT_MS             = 20_000;                                                                  // 20 seconds for server info
	private static final long KEEP_ALIVE_INTERVAL_SECONDS        = 45;                                                                      // 45 seconds for keep-alive
	private final Map<Long , JanusSession> sessions              = new ConcurrentHashMap<>();
	private final Map<Long , ScheduledFuture<?>> keepAliveTasks  = new ConcurrentHashMap<>();
	private final StringBuilder messageBuffer                    = new StringBuilder();
	private final JanusConfiguration config;
	private final HttpClient httpClient;
	private WebSocket webSocket;
	private final ExecutorService executor;
	
	private final ScheduledExecutorService keepAliveScheduler;
	
	private final TransactionManager transactionManager;
	
	
	public JanusClient(JanusConfiguration config) {
		this.config             = config;
		this.transactionManager = new TransactionManager();
		this.executor           = Executors.newVirtualThreadPerTaskExecutor();
		this.httpClient         = HttpClient.newBuilder().executor(this.executor).build();
		this.keepAliveScheduler = Executors.newScheduledThreadPool(1);
		
		try {
			logger.info("Starting connection attempt...");
			connect().get();
			logger.info("Connection established, retrieving server info...");
			ServerInfo serverInfo = getServerInfo().get();
			if (config.isLogEnabled()) {
				logger.info("Server Info:\n Janus={}, \nVersion={}, \nPlugins={}",
						serverInfo.janus(),
						serverInfo.versionString(),
						serverInfo.plugins().keySet());
			}
			
		} catch (InterruptedException e) {
			logger.info("Program interrupted, shutting down.");
			Thread.currentThread().interrupt(); // Restore interrupted status
		} catch (Exception e) {
			logger.error("Failed to connect or retrieve server info: {}", e.getMessage(), e);
		}
	}
	
	public CompletableFuture<Void> connect() {
		logger.info("Connecting to Janus Gateway at {}", config.getUri());
		CompletableFuture<Void> connectionFuture = httpClient.newWebSocketBuilder()
				                                           .subprotocols("janus-protocol")
				                                           .buildAsync(config.getUri(), this)
				                                           .thenAccept(ws -> this.webSocket = ws);
		
		return connectionFuture.orTimeout(DEFAULT_CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
				       .exceptionally(throwable -> {
					       logger.error("Failed to connect to Janus Gateway at {}: {}", config.getUri(), throwable.getMessage(), throwable);
					       throw new JanusException("Connection failed", throwable);
				       });
	}
	
	private void scheduleKeepAlive(long sessionId) {
		ScheduledFuture<?> keepAliveTask = keepAliveScheduler.scheduleAtFixedRate(() -> {
			try {
				JSONObject keepAlive = new JSONObject();
				keepAlive.put("janus", "keepalive");
				keepAlive.put("session_id", sessionId);
				keepAlive.put("transaction", transactionManager.createTransaction());
				sendMessage(keepAlive);
				if (config.isLogEnabled()) {
					logger.info("Sent keep-alive for session {}", sessionId);
				}
			} catch (Exception e) {
				logger.error("Failed to send keep-alive for session {}: {}", sessionId, e.getMessage(), e);
			}
		}, KEEP_ALIVE_INTERVAL_SECONDS, KEEP_ALIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
		keepAliveTasks.put(sessionId, keepAliveTask);
		logger.debug("Scheduled keep-alive task for session {}", sessionId);
	}
	
	public void disconnect() {
		// 1. Stop keep-alive tasks and shut down the scheduler
		logger.info("Shutting down keep-alive scheduler...");
		keepAliveTasks.values().forEach(task -> task.cancel(false));
		keepAliveTasks.clear();
		keepAliveScheduler.shutdown();
		
		// 2. Close WebSocket connection
		if (webSocket != null && !webSocket.isOutputClosed()) {
			try {
				webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client requested disconnect").get(5, TimeUnit.SECONDS);
				logger.info("Disconnected from Janus Gateway at {}", config.getUri());
			} catch (Exception e) {
				logger.warn("Error during graceful WebSocket disconnect: {}", e.getMessage());
			}
		}
		if (config.isLogEnabled()) {
			// 3. Shut down the main executor
			logger.info("Shutting down main executor...");
		}
		executor.shutdown();
		
		// 4. Await termination of both schedulers
		try {
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
			if (!keepAliveScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
				keepAliveScheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			keepAliveScheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public void onOpen(WebSocket webSocket) {
		logger.info("Successfully connected to Janus Gateway at {}", config.getUri());
		webSocket.request(1);
	}
	
	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		if (config.isLogEnabled()) {
			logger.info("Received WebSocket message fragment: length={}, last={}", data.length(), last);
		}
		messageBuffer.append(data);
		
		if (last) {
			String completeMessage = messageBuffer.toString();
			if (config.isLogEnabled()) {
				logger.debug("Message From Janus: {}", completeMessage);
			}
			executor.submit(() -> processMessage(completeMessage));
			messageBuffer.setLength(0); // Clear buffer after processing
		} else {
			logger.debug("Waiting for more fragments, current buffer length: {}", messageBuffer.length());
		}
		
		webSocket.request(1);
		return CompletableFuture.completedFuture(null);
	}
	
	private void processMessage(String message) {
		try {
			if (message == null || message.trim().isEmpty()) {
				logger.warn("Received empty or null message, skipping.");
				return;
			}
			if (!message.trim().startsWith("{")) {
				logger.warn("Received invalid JSON message, does not start with '{{': {}", message);
				return;
			}
			
			JSONObject json = new JSONObject(message);
			//	logger.info("Processing JSON: {}", json.toString(2));
			
			String transactionId = json.optString("transaction", null);
			if (transactionId != null && !transactionId.isEmpty()) {
				logger.info("Found transaction ID: {}", transactionId);
				transactionManager.completeTransaction(transactionId, json);
				return;
			}
			
			long sessionId = json.optLong("session_id", -1);
			if (sessionId != -1) {
				Optional.ofNullable(sessions.get(sessionId)).ifPresent(session -> session.handleEvent(json));
			} else {
				logger.info("Received message with no transaction ID or session ID: {}", message);
			}
		} catch (JSONException e) {
			logger.error("Error parsing JSON message: {}", e.getMessage(), e);
			logger.debug("Problematic message: {}", message);
		} catch (Exception e) {
			logger.error("Unexpected error processing message: {}", e.getMessage(), e);
		}
	}
	
	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		logger.error("WebSocket error at {}: {}", config.getUri(), error.getMessage(), error);
		sessions.values().forEach(JanusSession::destroy);
		sessions.clear();
	}
	
	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		logger.warn("WebSocket closed for {}: {} - {}", config.getUri(), statusCode, reason);
		sessions.values().forEach(JanusSession::destroy);
		sessions.clear();
		return CompletableFuture.completedFuture(null);
	}
	
	public CompletableFuture<JanusSession> createSession() {
		String transactionId = transactionManager.createTransaction();
		var future = transactionManager.registerTransaction(transactionId);
		
		JSONObject request = new JSONObject();
		request.put("janus", "create");
		request.put("transaction", transactionId);
		
		sendMessage(request);
		
		return future.thenApply(response -> {
			long sessionId = response.getJSONObject("data").getLong("id");
			JanusSession session = new JanusSession(this, sessionId);
			sessions.put(sessionId, session);
			if (config.isLogEnabled()) {
				logger.info("Session created, session ID={}", sessionId);
			}
			scheduleKeepAlive(sessionId);
			return session;
		});
	}
	
	public CompletableFuture<ServerInfo> getServerInfo() {
		String transactionId = transactionManager.createTransaction();
		var future = transactionManager.registerTransaction(transactionId);
		
		JSONObject request = new JSONObject();
		request.put("janus", "info");
		request.put("transaction", transactionId);
		if(config.isLogEnabled()) {
			logger.info("Sending server info request: {}", request.toString());
		}
		sendMessage(request);
		return future.orTimeout(SERVER_INFO_TIMEOUT_MS, TimeUnit.MILLISECONDS)
				       .thenApply(JanusUtils::convertToServerInfo)
				       .exceptionally(throwable -> {
					       if (throwable instanceof TimeoutException) {
						       logger.error("Server info request timed out after {}ms. Server may not be responding.", SERVER_INFO_TIMEOUT_MS);
					       } else {
						       logger.error("Failed to retrieve server info from {}: {}", config.getUri(), throwable.getMessage(), throwable);
					       }
					       throw new JanusException("Failed to retrieve server info", throwable);
				       });
	}
	
	
	
	public void sendMessage(JSONObject message) {
		if (webSocket == null || webSocket.isOutputClosed()) {
			logger.error("Cannot send message: WebSocket is not connected to {}", config.getUri());
			throw new IllegalStateException("WebSocket is not connected.");
		}
		String msgStr = message.toString();
		if(config.isLogEnabled()) {
			logger.info("Sending message: {}", msgStr);
		}
		webSocket.sendText(msgStr, true);
	}
	
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}
	
	void removeSession(long sessionId) {
		sessions.remove(sessionId);
	}
}