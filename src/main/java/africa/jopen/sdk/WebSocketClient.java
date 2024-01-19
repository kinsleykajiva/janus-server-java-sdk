package africa.jopen.sdk;

import java.net.URI;

public interface WebSocketClient {
	
	
	void send(String message);
	
	void close();
}
