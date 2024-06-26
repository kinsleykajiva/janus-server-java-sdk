package io.github.kinsleykajiva.cache.mongodb;

import io.github.kinsleykajiva.cache.DatabaseConfig;

public record MongoConfiguration(String host, Integer port, String database, String username, String password) implements DatabaseConfig {
	public MongoConfiguration {
		if (host == null || host.isEmpty()) {
			host = "localhost";
		}
		if (port == null) {
			port = 27017;
		}
		
		if (database == null || database.isEmpty()) {
			new IllegalArgumentException("Database cannot be null");
		}
		if (username == null) {
			username = "";
		}
		if (password == null) {
			password = "";
		}
	}
	
	@Override
	public String getHost() {
		return host();
	}
	
	@Override
	public int getPort() {
		return port();
	}
	
	@Override
	public String getDatabase() {
		return database();
	}
	
	@Override
	public String getUsername() {
		return username();
	}
	
	@Override
	public String getPassword() {
		return password();
	}
}
