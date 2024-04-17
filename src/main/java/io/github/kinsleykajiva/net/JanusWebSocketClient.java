package io.github.kinsleykajiva.net;

import io.github.kinsleykajiva.utils.JanusEventHandler;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

public class JanusWebSocketClient implements WebSocketClient {
	static        Logger            log = Logger.getLogger(JanusWebSocketClient.class.getName());
	private       WebSocket         webSocket;
	private final String            url;
	private       Thread            thread;
	private final JanusEventHandler eventHandler;
	
	
	public JanusWebSocketClient( String url, JanusEventHandler eventHandler ) {
		this.eventHandler = eventHandler;
		this.url = url;
	}
	
	public void initializeWebSocket() {
		try {
			HttpClient        httpClient       = HttpClient.newBuilder().build();
			WebSocket.Builder webSocketBuilder = httpClient.newWebSocketBuilder();
			webSocketBuilder.subprotocols("janus-protocol");
			webSocketBuilder.connectTimeout(Duration.of(10, ChronoUnit.SECONDS));
			webSocketBuilder.buildAsync(URI.create(url), new WebSocketHandler()).join();
		} catch (Exception e) {
			log.severe("Failed to initialize WebSocket: " + e.getMessage());
		}
		
	}
	
	
	@Override
	public void send( String message ) {
		log.info("xxxSending message: " + message);
		webSocket.sendText(message, true);
	}
	
	@Override
	public void close() {
	
	}
	
	private class WebSocketHandler implements WebSocket.Listener {
		private final StringBuffer buffer = new StringBuffer();
		
		@Override
		public void onOpen( WebSocket ws ) {
			webSocket= ws;
			WebSocket.Listener.super.onOpen(ws);
			eventHandler.onConnected();
		}
		
		@Override
		public CompletionStage<?> onText( WebSocket ws, CharSequence data, boolean last ) {
			
			if (last) {
				buffer.append(data);
				log.info("Received message: " + buffer);
				eventHandler.handleEvent(new JSONObject(buffer.toString()));
				buffer.setLength(0); // Clear the buffer
			}
			return WebSocket.Listener.super.onText(ws, data, last);
		}
		
		@Override
		public CompletionStage<?> onClose( WebSocket webSocket, int statusCode, String reason ) {
			log.info("WebSocket closed: " + statusCode + ", " + reason);
			return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
		}
		
		@Override
		public void onError( WebSocket webSocket, Throwable error ) {
			error.printStackTrace();
			log.severe("WebSocket error: " + error.getMessage());
			WebSocket.Listener.super.onError(webSocket, error);
		}
	}
}
