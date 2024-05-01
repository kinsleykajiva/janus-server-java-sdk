package io.github.kinsleykajiva.cache;

public interface DatabaseConnection {

  public void connect();

  public void disconnect();

  public void executeDBActionCommand(String sqlStringOrDocumentObjectString);
}
