package io.github.kinsleykajiva.admin_ui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {

    private final String username;
    private final String password;
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>(); // Token -> Username

    public AuthService(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password cannot be null or empty.");
        }
        this.username = username;
        this.password = password;
    }

    public String login(String username, String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            String token = UUID.randomUUID().toString();
            activeSessions.put(token, username);
            return token;
        }
        return null;
    }

    public void logout(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }

    public boolean validateToken(String token) {
        return token != null && activeSessions.containsKey(token);
    }
}
