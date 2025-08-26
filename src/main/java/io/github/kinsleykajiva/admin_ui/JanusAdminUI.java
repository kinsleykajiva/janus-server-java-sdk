package io.github.kinsleykajiva.admin_ui;

import io.github.kinsleykajiva.cache.CacheService;
import io.github.kinsleykajiva.janus.admin.JanusAdminClient;
import io.github.kinsleykajiva.janus.admin.JanusAdminConfiguration;

import java.io.IOException;

public class JanusAdminUI {
    private final AdminWebApp adminWebApp;
    private final JanusAdminClient janusAdminClient;
    private final CacheService cacheService;
    private final AuthService authService;

    public JanusAdminUI(JanusAdminConfiguration adminConfig) throws IOException {
        this(adminConfig, "cache", 0, null, null);
    }

    public JanusAdminUI(JanusAdminConfiguration adminConfig, String cachePath, int uiPort, String username, String password) throws IOException {
        this.cacheService = new CacheService(cachePath);
        if (username != null && password != null) {
            this.authService = new AuthService(username, password);
        } else {
            this.authService = null;
        }
        this.adminWebApp = new AdminWebApp(uiPort, this.cacheService, this.authService);
        this.janusAdminClient = new JanusAdminClient(adminConfig, this.cacheService);

        this.janusAdminClient.getAdminMonitor().addListener(event -> {
            adminWebApp.broadcast(event.toString());
        });
    }

    public void start() {
        adminWebApp.start();
    }

    public void stop() {
        adminWebApp.stop();
        janusAdminClient.disconnect();
    }

    public JanusAdminClient getJanusAdminClient() {
        return janusAdminClient;
    }
}
