package io.github.kinsleykajiva;

import io.github.kinsleykajiva.janus.JanusConfiguration;
import io.github.kinsleykajiva.janus.JanusClient;
import io.github.kinsleykajiva.janus.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		// Enable debug logging for more details
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
		
		// Configure the Janus client
		JanusConfiguration config = new JanusConfiguration(
				"",
				8188,
				"/janus",
				false, // Use ws (non-secure) as per logs
				true   // Logging is ON
		);
		
		JanusClient client = new JanusClient(config);
		
		// Add shutdown hook to ensure proper cleanup
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Shutting down JanusClient...");
			client.disconnect();
		}));
		try{
			client.createSession().get();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		/*try {
			// Connect and wait for completion
			logger.info("Starting connection attempt...");
			client.connect().get(15, TimeUnit.SECONDS);
			logger.info("Connection established, retrieving server info...");
			
			// Get server info
			ServerInfo serverInfo = client.getServerInfo().get();
			logger.info("Server Info: Janus={}, Version={}, Plugins={}",
					serverInfo.janus(),
					serverInfo.versionString(),
					serverInfo.plugins().keySet());
			
			// Keep the program running to maintain the WebSocket connection
			logger.info("Connection active. Press Ctrl+C to exit.");
			Thread.currentThread().join(); // Blocks until interrupted
			
		} catch (TimeoutException e) {
			logger.error("Operation timed out: {}", e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.info("Program interrupted, shutting down.");
			Thread.currentThread().interrupt(); // Restore interrupted status
		} catch (Exception e) {
			logger.error("Failed to connect or retrieve server info: {}", e.getMessage(), e);
		} finally {
			client.disconnect();
		}*/
	}
}