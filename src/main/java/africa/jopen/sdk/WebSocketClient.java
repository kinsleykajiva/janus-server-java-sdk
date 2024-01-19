package africa.jopen.sdk;

import java.net.URI;

public interface WebSocketClient {
	void connect( URI uri, JanusEventHandler eventHandler);
	
	void send(String message);
	
	void close();
}
