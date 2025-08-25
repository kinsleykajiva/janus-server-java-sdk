package io.github.kinsleykajiva;

import io.github.kinsleykajiva.janus.JanusClient;
import io.github.kinsleykajiva.janus.JanusConfiguration;
import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.ServerInfo;
import io.github.kinsleykajiva.janus.event.JanusEvent;
import io.github.kinsleykajiva.janus.event.JanusSipEventListener;
import io.github.kinsleykajiva.janus.event.JanusSipEvents;
import io.github.kinsleykajiva.janus.handle.impl.SipHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		// Enable debug logging for more details
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");
		
		// Configure the Janus client
		JanusConfiguration config = new JanusConfiguration(
				"***.**.**.**", // Replace with your Janus server IP
				8188,
				"/janus",
				false,
				true
		);
		
		JanusClient client = new JanusClient(config);
		
		// Add shutdown hook to ensure proper cleanup
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Shutting down JanusClient...");
			client.disconnect();
		}));
		
		try {
			// 1. Connect to Janus
			logger.info("Connecting to Janus server at {}...", config.getUri());
			client.connect().get(10, TimeUnit.SECONDS);
			
			// 2. Get Server Info
			ServerInfo serverInfo = client.getServerInfo().get();
			logger.info("Server Info: Janus v{}", serverInfo.versionString());

			// 3. Create a Session
			logger.info("Creating Janus session...");
			JanusSession session = client.createSession().get();
			logger.info("Session created with ID: {}", session.getSessionId());

			// 4. Attach to the SIP Plugin
			logger.info("Attaching to SIP plugin...");
			SipHandle sipHandle = session.attachSipPlugin().get();
			logger.info("SIP handle attached with ID: {}", sipHandle.getHandleId());

			// 5. Add a persistent listener for SIP events like incoming calls
			sipHandle.addListener(new JanusSipEventListener() {
				@Override
				public void onIncomingCallEvent(JanusSipEvents.InComingCallEvent event) {
					logger.info("xxxxxIncoming call from: {} (Call-ID: {})", event.displayName(), event.callId());
					// Here you would typically handle the call, e.g., by answering it.
					// sipHandle.answerCallAsync(event.jsep());
				}
				
				@Override
				public void onRegisteredEvent(JanusSipEvents.SuccessfulRegistration event) {
					JanusSipEventListener.super.onRegisteredEvent(event);
					logger.info("xxxxxxSIP registration successful for: {}", event.username());
				}
				
				@Override
				public void onFailedRegistrationEvent(JanusSipEvents.ErrorRegistration event) {
					JanusSipEventListener.super.onFailedRegistrationEvent(event);
					logger.error("xxxxSIP registration failed: {} ({})", event.reason(), event.code());
				}
				
				@Override
				public void onHangupCallEvent(JanusSipEvents.HangupEvent event) {
					logger.info("xxxxCall hung up: {} (Reason: {})", event.callId(), event.reason());
				}

				@Override
				public void onEvent(JanusEvent event) {
					// This is the generic event handler, still needs to be implemented.
					// You can inspect the raw event data here if needed.
					// logger.debug("Received generic SIP event: {}", event.eventData());
				}
			});

			// 6. Register a SIP user (a one-time action)
			logger.info("Registering SIP user...");
			var registrationResult = sipHandle.registerAsync("********","***.**","***.**.**.**").get();

			/*if (registrationResult instanceof JanusSipEvents.SuccessfulRegistration success) {
				logger.info("SIP registration successful for: {}", success.username());
			} else if (registrationResult instanceof JanusSipEvents.ErrorRegistration error) {
				logger.error("SIP registration failed: {} ({})", error.reason(), error.code());
			}
			*/
			// 7. Keep the application running to listen for events
			logger.info("Setup complete. Listening for SIP events. Press Ctrl+C to exit.");
			Thread.currentThread().join(); // Block forever
			
		} catch (Exception e) {
			logger.error("An error occurred: {}", e.getMessage(), e);
		} finally {
			logger.info("Disconnecting client.");
			client.disconnect();
		}
	}
}