package io.github.kinsleykajiva.admin_ui;

import com.google.gson.Gson;
import io.github.kinsleykajiva.cache.CacheService;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import io.javalin.websocket.WsContext;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdminWebApp {

    private final Javalin app;
    private final Set<WsContext> wsClients = Collections.synchronizedSet(new HashSet<>());

    public AdminWebApp(int port, CacheService cacheService, AuthService authService) {

        // Configure Gson for JSON mapping
        Gson gson = new Gson();
        JsonMapper gsonMapper = new JsonMapper() {
            @Override
            public String toJsonString(Object obj, Type type) {
                return gson.toJson(obj, type);
            }

            @Override
            public <T> T fromJsonString(String json, Type type) {
                return gson.fromJson(json, type);
            }
        };

        // Create Javalin app
        this.app = Javalin.create(config -> {
            config.jsonMapper(gsonMapper);
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/admin_ui";
                staticFiles.precompress = false; // Disable for simplicity
            });
        });

        // Authentication Filter (Before Handler)
        app.before(ctx -> {
            if (authService == null) {
                return; // No auth configured, allow all
            }
            String path = ctx.path();
            if (path.equals("/login.html") || path.startsWith("/css/") || path.startsWith("/js/") || path.equals("/api/login")) {
                return; // Allow access to login page and its assets
            }

            String token = ctx.cookie("session_token");
            if (!authService.validateToken(token)) {
                ctx.redirect("/login.html");
                ctx.status(401);
            }
        });

        // API Handlers
        app.post("/api/login", ctx -> {
            if (authService == null) {
                ctx.status(500).json(Map.of("error", "Authentication not configured"));
                return;
            }
            var user = ctx.bodyAsClass(Map.class);
            String token = authService.login((String) user.get("username"), (String) user.get("password"));
            if (token != null) {
                ctx.cookie("session_token", token);
                ctx.json(Map.of("token", token));
            } else {
                ctx.status(401).json(Map.of("error", "Invalid credentials"));
            }
        });

        app.post("/api/logout", ctx -> {
            if (authService != null) {
                authService.logout(ctx.cookie("session_token"));
            }
            ctx.removeCookie("session_token");
            ctx.json(Map.of("message", "Logged out"));
        });

        app.get("/api/events", ctx -> {
            ctx.json(cacheService.getEvents());
        });

        app.get("/api/history", ctx -> {
             ctx.json(cacheService.getEvents());
        });

        app.post("/api/events/clear", ctx -> {
            cacheService.clearCache();
            ctx.json(Map.of("message", "Cache cleared"));
        });

        // WebSocket Handler
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                if (authService != null) {
                    String token = ctx.queryParam("token");
                    if (!authService.validateToken(token)) {
                        ctx.closeSession(1008, "Unauthorized"); // 1008 = Policy Violation
                        return;
                    }
                }
                ctx.enableAutomaticPings();
                wsClients.add(ctx);
            });
            ws.onClose(ctx -> {
                wsClients.remove(ctx);
            });
        });

        // Start server
        if (port > 0) {
            this.app.start(port);
        } else {
            this.app.start(0); // start on random port
        }
    }

    public void start() {
        System.out.println("Admin Web App started on port " + app.port());
    }

    public int getPort() {
        return app.port();
    }

    public void stop() {
        app.stop();
    }

    public void broadcast(String message) {
        wsClients.forEach(client -> {
            if (client.session.isOpen()) {
                client.send(message);
            }
        });
    }
}
