package io.github.kinsleykajiva.admin_ui;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import io.javalin.websocket.WsContext;
import io.github.kinsleykajiva.cache.CacheService;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AdminWebApp {

    private final Javalin app;
    private final CacheService cacheService;
    private final AuthService authService;
    private final Set<WsContext> wsClients = Collections.synchronizedSet(new HashSet<>());

    public AdminWebApp(int port, CacheService cacheService, AuthService authService) {
        this.cacheService = cacheService;
        this.authService = authService;

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

        this.app = Javalin.create(config -> {
            config.jsonMapper(gsonMapper);
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/admin_ui";
            });

            // Security filter
            config.accessManager((handler, ctx, permittedRoles) -> {
                if (authService == null) {
                    handler.handle(ctx);
                    return;
                }

                String path = ctx.path();
                if (path.equals("/login.html") || path.equals("/api/login") || path.startsWith("/css/") || path.startsWith("/js/")) {
                    handler.handle(ctx);
                    return;
                }

                String token = ctx.cookie("session_token");
                if (authService.validateToken(token)) {
                    handler.handle(ctx);
                } else {
                    ctx.redirect("/login.html");
                }
            });
        });

        app.routes(() -> {
            path("/api", () -> {
                post("/login", ctx -> {
                    if (authService == null) {
                        ctx.status(500).json("{\"error\":\"Authentication not configured\"}");
                        return;
                    }
                    var user = ctx.bodyAsClass(Map.class);
                    String token = authService.login((String) user.get("username"), (String) user.get("password"));
                    if (token != null) {
                        ctx.cookie("session_token", token);
                        ctx.json("{\"token\":\"" + token + "\"}");
                    } else {
                        ctx.status(401).json("{\"error\":\"Invalid credentials\"}");
                    }
                });

                post("/logout", ctx -> {
                    if (authService != null) {
                        authService.logout(ctx.cookie("session_token"));
                    }
                    ctx.removeCookie("session_token");
                    ctx.json("{\"message\":\"Logged out\"}");
                });

                get("/events", ctx -> {
                    ctx.json(cacheService.getEvents());
                });
                get("/history", ctx -> {
                    // For now, returns all events. Will be enhanced with filtering.
                    ctx.json(cacheService.getEvents());
                });
                post("/events/clear", ctx -> {
                    cacheService.clearCache();
                    ctx.json("{\"message\":\"Cache cleared\"}");
                });
            });
        }).ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                if (authService != null) {
                    String token = ctx.queryParam("token");
                    if (!authService.validateToken(token)) {
                        ctx.closeSession(401, "Unauthorized");
                        return;
                    }
                }
                wsClients.add(ctx);
            });
            ws.onClose(ctx -> {
                wsClients.remove(ctx);
            });
        });

        if (port > 0) {
            this.app.start(port);
        } else {
            this.app.start(0); // start on random port
        }
    }

    public void start() {
        // The server is already started in the constructor.
        // This method can be used for logging or other startup logic.
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
            client.send(message);
        });
    }
}
