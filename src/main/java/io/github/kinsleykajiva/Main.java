package io.github.kinsleykajiva;

import io.github.kinsleykajiva.janus.JanusClient;
import io.github.kinsleykajiva.janus.JanusConfiguration;
import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.ServerInfo;
import io.github.kinsleykajiva.janus.handle.impl.AudioBridgeHandle;
import io.github.kinsleykajiva.janus.plugins.audiobridge.events.*;
import io.github.kinsleykajiva.janus.plugins.audiobridge.listeners.JanusAudioBridgeListener;
import io.github.kinsleykajiva.janus.plugins.audiobridge.models.CreateRoomRequest;
import io.github.kinsleykajiva.janus.plugins.audiobridge.models.JoinRoomRequest;
import org.json.JSONObject;
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
		        "localhost", // Replace with your Janus server IP
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

            // 4. Attach to the AudioBridge Plugin
            logger.info("Attaching to AudioBridge plugin...");
            AudioBridgeHandle audioBridgeHandle = session.attachAudioBridgePlugin().get();
            logger.info("AudioBridge handle attached with ID: {}", audioBridgeHandle.getHandleId());

            // 5. Add a listener for AudioBridge events
            audioBridgeHandle.addAudioBridgeListener(new JanusAudioBridgeListener() {
                @Override
                public void onJoined(JoinedEvent event) {
                    logger.info("Successfully joined room {}. My ID is {}. Participants: {}",
                        event.roomId(), event.participantId(), event.participants());
                }

                @Override
                public void onParticipantJoined(ParticipantJoinedEvent event) {
                    logger.info("Participant {} joined room {}", event.participant().display(), event.roomId());
                }

                @Override
                public void onParticipantLeft(ParticipantLeftEvent event) {
                    logger.info("Participant {} left room {}", event.participantId(), event.roomId());
                }

                @Override
                public void onParticipantUpdated(ParticipantUpdatedEvent event) {
                     logger.info("Participant {} in room {} was updated. Muted: {}",
                        event.participant().display(), event.roomId(), event.participant().muted());
                }

                @Override
                public void onRoomDestroyed(RoomDestroyedEvent event) {
                    logger.info("Room {} was destroyed.", event.roomId());
                }

                @Override
                public void onEvent(JSONObject event) {
                    // Raw event for debugging
                    logger.debug("Received raw AudioBridge event: {}", event.toString(2));
                }
            });

            // 6. Create a new room
            logger.info("Creating a new audio room...");
            var createRoomRequest = new CreateRoomRequest.Builder()
                .setDescription("My awesome new room")
                .setIsPrivate(false)
                .build();
            var room = audioBridgeHandle.createRoom(createRoomRequest).get();
            logger.info("Room created with ID: {}", room.room());

            // 7. Join the room
            logger.info("Joining room {}...", room.room());
            var joinRoomRequest = new JoinRoomRequest.Builder(room.room())
                .setDisplay("Jules")
                .setMuted(false)
                .build();
            audioBridgeHandle.joinRoom(joinRoomRequest).get();

            // Give Janus a moment to process the join and send the event
            Thread.sleep(1000);

            // 8. List participants in the room
            logger.info("Listing participants...");
            var participants = audioBridgeHandle.listParticipants(room.room()).get();
            logger.info("Participants in room {}: {}", room.room(), participants);


            // 9. Keep the application running to listen for more events
            logger.info("Setup complete. Listening for AudioBridge events. Press Ctrl+C to exit.");
            Thread.currentThread().join(); // Block forever

        } catch (Exception e) {
            logger.error("An error occurred: {}", e.getMessage(), e);
        } finally {
            logger.info("Disconnecting client.");
            client.disconnect();
        }
    }
}