package io.github.kinsleykajiva.utils;

public interface WebSocketClient {
	
	
	void send(String message);
	
	void close();
}
