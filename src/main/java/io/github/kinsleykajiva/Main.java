package io.github.kinsleykajiva;

import io.github.kinsleykajiva.admin_ui.JanusAdminUI;
import io.github.kinsleykajiva.janus.admin.JanusAdminConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Enable info logging for more details
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");

        // 1. Configure the Janus Admin client
        // Replace with your Janus admin websocket URI and secret
        JanusAdminConfiguration adminConfig = new JanusAdminConfiguration(
            URI.create("ws://localhost:7188/janus"),
            "janusoverlord"
        );

        try {
            // 2. Initialize the Admin UI feature
            // This will start caching admin events and prepare the web server.
            // Using default constructor:
            // - Cache folder: 'cache/' in the application's root directory.
            // - Web UI port: A random available port.
            // - Authentication: Disabled by default.
            logger.info("Initializing Janus Admin UI...");

            // To enable authentication, provide a username and password
            final String username = "admin";
            final String password = "password";
            logger.info("Admin UI credentials: username='{}', password='{}'", username, password);
            final JanusAdminUI adminUI = new JanusAdminUI(adminConfig, "cache", 0, username, password);

            // To run without authentication:
            // final JanusAdminUI adminUI = new JanusAdminUI(adminConfig);


            // 3. Add a shutdown hook for graceful exit
            // This ensures the web server and client are disconnected properly.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down Janus Admin UI...");
                adminUI.stop();
            }));

            // 4. Start the UI server
            // The server will run in the background.
            adminUI.start();
            logger.info("Admin UI server has been started. Check the console logs above for the exact port number.");
            logger.info("You can now access the UI in your browser.");
            logger.info("Press Enter in this console to stop the application.");

            // 5. Keep the application running
            // The web server is on a background thread, so we need to prevent the main thread from exiting.
            System.in.read();

        } catch (Exception e) {
            logger.error("An error occurred during application startup: {}", e.getMessage(), e);
        } finally {
            logger.info("Application shutting down.");
        }
    }
}