package io.github.kinsleykajiva.admin_ui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.kinsleykajiva.cache.CacheService;
import java.io.IOException;
import java.io.OutputStream;

public class EventsHandler implements HttpHandler {

    private final CacheService cacheService;

    public EventsHandler(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            handleGetRequest(exchange);
        } else if ("POST".equalsIgnoreCase(method)) {
            handlePostRequest(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        String response = cacheService.getEvents();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/api/events/clear".equals(path)) {
            cacheService.clearCache();
            String response = "{\"message\":\"Cache cleared\"}";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(404, -1); // Not Found
        }
    }
}
