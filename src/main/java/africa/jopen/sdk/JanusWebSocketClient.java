package africa.jopen.sdk;

import org.json.JSONObject;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

public class JanusWebSocketClient implements WebSocketClient{
	static Logger    log = Logger.getLogger(JanusWebSocketClient.class.getName());
	private    WebSocket webSocket;
	private    String    url;
	private final JanusEventHandler eventHandler;
	
	public JanusWebSocketClient(String url,JanusEventHandler eventHandler) {
		
		this.eventHandler = eventHandler;
		initializeWebSocket(URI.create(url));
	}
	private void initializeWebSocket(URI uri) {
		try {
			HttpClient httpClient = HttpClient.newBuilder().build();
			WebSocket.Builder webSocketBuilder = httpClient.newWebSocketBuilder();
			webSocketBuilder.subprotocols("janus-protocol");
			webSocketBuilder.connectTimeout(Duration.of(10, ChronoUnit.SECONDS));
			webSocket = webSocketBuilder.buildAsync(uri, new WebSocketHandler()).join();
		} catch (Exception e) {
			log.severe("Failed to initialize WebSocket: " + e.getMessage());
			// Handle exception appropriately or rethrow if necessary
		}
	}
	
	@Override
	public void connect( URI uri, JanusEventHandler eventHandler ) {
	
	}
	
	@Override
	public void send( String message ) {
	
	}
	
	@Override
	public void close() {
	
	}
	
	private class WebSocketHandler implements WebSocket.Listener {
		private StringBuffer buffer = new StringBuffer();
		
		@Override
		public void onOpen( WebSocket webSocket ) {
			WebSocket.Listener.super.onOpen(webSocket);
		}
		
		@Override
		public CompletionStage<?> onText( WebSocket webSocket, CharSequence data, boolean last ) {
			if (last) {
				buffer.append(data);
				StringReader reader = new StringReader(buffer.toString());
				eventHandler.handleEvent(new JSONObject(reader.toString()));
				buffer.setLength(0); // Clear the buffer
			}
			return WebSocket.Listener.super.onText(webSocket, data, last);
		}
		
		@Override
		public CompletionStage<?> onClose( WebSocket webSocket, int statusCode, String reason ) {
			return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
		}
		
		@Override
		public void onError( WebSocket webSocket, Throwable error ) {
			WebSocket.Listener.super.onError(webSocket, error);
		}
	}
}
