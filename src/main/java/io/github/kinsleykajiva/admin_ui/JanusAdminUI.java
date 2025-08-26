package io.github.kinsleykajiva.admin_ui;

import io.github.kinsleykajiva.cache.CacheService;
import io.github.kinsleykajiva.janus.admin.JanusAdminClient;
import io.github.kinsleykajiva.janus.admin.JanusAdminConfiguration;

import java.io.IOException;

public class JanusAdminUI {
    private final AdminUIServer adminUIServer;
    private final JanusAdminClient janusAdminClient;
    private final CacheService cacheService;

    public JanusAdminUI(JanusAdminConfiguration adminConfig) throws IOException {
        this(adminConfig, "cache", 0);
    }

    public JanusAdminUI(JanusAdminConfiguration adminConfig, String cachePath) throws IOException {
        this(adminConfig, cachePath, 0);
    }

    public JanusAdminUI(JanusAdminConfiguration adminConfig, int uiPort) throws IOException {
        this(adminConfig, "cache", uiPort);
    }

    public JanusAdminUI(JanusAdminConfiguration adminConfig, String cachePath, int uiPort) throws IOException {
        this.cacheService = new CacheService(cachePath);
        this.adminUIServer = new AdminUIServer(uiPort, this.cacheService);
        this.janusAdminClient = new JanusAdminClient(adminConfig, this.cacheService);
    }

    public void start() {
        adminUIServer.start();
    }

    public void stop() {
        adminUIServer.stop();
        janusAdminClient.disconnect();
    }

    public JanusAdminClient getJanusAdminClient() {
        return janusAdminClient;
    }
}
