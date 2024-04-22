package io.github.kinsleykajiva.cache;

public interface DatabaseConfig {
	String getHost();
	int getPort();
	String getDatabase();
	String getUsername();
	String getPassword();
}
