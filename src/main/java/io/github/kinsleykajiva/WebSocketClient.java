package io.github.kinsleykajiva;

public interface WebSocketClient {
	
	
	void send(String message);
	
	void close();
}
