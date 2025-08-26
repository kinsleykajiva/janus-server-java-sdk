package io.github.kinsleykajiva.admin_ui;

import com.sun.net.httpserver.HttpServer;
import io.github.kinsleykajiva.cache.CacheService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

public class AdminUIServer {

    private final HttpServer server;
    private final int port;

    public AdminUIServer(int port, CacheService cacheService) throws IOException {
        if (port == 0) {
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                this.port = serverSocket.getLocalPort();
            }
        } else {
            this.port = port;
        }
        server = HttpServer.create(new InetSocketAddress(this.port), 0);
        server.createContext("/api/events", new EventsHandler(cacheService));
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(Executors.newCachedThreadPool());
    }

    public void start() {
        server.start();
        System.out.println("Admin UI server started on port " + port);
    }

    public void stop() {
        server.stop(0);
        System.out.println("Admin UI server stopped.");
    }
}
