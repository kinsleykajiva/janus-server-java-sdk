package io.github.kinsleykajiva.admin_ui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class StaticFileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) {
            path = "/index.html";
        }

        try (InputStream in = getClass().getResourceAsStream("/admin_ui" + path)) {
            if (in == null) {
                exchange.sendResponseHeaders(404, -1); // Not Found
                return;
            }

            exchange.sendResponseHeaders(200, 0);
            try (OutputStream out = exchange.getResponseBody()) {
                in.transferTo(out);
            }
        }
    }
}
