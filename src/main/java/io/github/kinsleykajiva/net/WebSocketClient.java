package io.github.kinsleykajiva.net;

public interface WebSocketClient {

  void send(String message);

  void close();
}
