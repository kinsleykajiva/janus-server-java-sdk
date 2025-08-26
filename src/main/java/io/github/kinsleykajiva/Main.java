package io.github.kinsleykajiva;

import io.github.kinsleykajiva.janus.JanusClient;
import io.github.kinsleykajiva.janus.JanusConfiguration;
import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.ServerInfo;
import io.github.kinsleykajiva.janus.admin.JanusAdminClient;
import io.github.kinsleykajiva.janus.admin.JanusAdminConfiguration;
import io.github.kinsleykajiva.janus.admin.messages.ListSessionsResponse;
import io.github.kinsleykajiva.janus.handle.impl.VideoRoomHandle;
import io.github.kinsleykajiva.janus.plugins.videoroom.events.*;
import io.github.kinsleykajiva.janus.plugins.videoroom.listeners.JanusVideoRoomListener;
import io.github.kinsleykajiva.janus.plugins.videoroom.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
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

        JanusAdminConfiguration adminConfig = new JanusAdminConfiguration(
            URI.create("ws://localhost:7188/janus"),
            "janusoverlord"
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

            // Run the VideoRoom example
            // runVideoRoomExample(session);

            // Run the Admin Client Example
            runAdminClientExample(adminConfig);


            // Keep the application running to listen for more events
            logger.info("Example finished. Application will exit in 10 seconds.");
            Thread.sleep(10000);

        } catch (Exception e) {
            logger.error("An error occurred: {}", e.getMessage(), e);
        } finally {
            logger.info("Disconnecting client.");
            client.disconnect();
        }
    }

    /**
     * A comprehensive example demonstrating the VideoRoomHandle workflow.
     * @param session An active JanusSession.
     * @throws Exception if any operation fails.
     */
    public static void runVideoRoomExample(JanusSession session) throws Exception {
        System.out.println("\n--- Running VideoRoom Example ---\n");

        // 1. Attach to the VideoRoom Plugin
        // This creates a handle, which is a context for all subsequent plugin interactions.
        System.out.println("Attaching to VideoRoom plugin...");
        VideoRoomHandle videoRoomHandle = session.attachToVideoRoom().get();
        System.out.println("VideoRoom handle attached with ID: " + videoRoomHandle.getHandleId());

        // 2. Add a listener for VideoRoom events
        // This is crucial for handling asynchronous responses from the plugin.
        videoRoomHandle.addVideoRoomListener(new JanusVideoRoomListener() {
            @Override
            public void onJoined(JoinedEvent event) {
                System.out.printf(">>> Successfully joined room %d as a publisher (My ID: %d)%n",
                    event.room(), event.id());
                if (event.publishers().isEmpty()) {
                    System.out.println(">>> There are no other active publishers in the room.");
                } else {
                    event.publishers().forEach(publisher ->
                        System.out.printf(">>> Active publisher in room: %s (ID: %d)%n",
                            publisher.display(), publisher.id()));
                }
            }
			
			
			
			

            @Override
            public void onPublisherAdded(PublisherAddedEvent event) {
                event.publishers().forEach(publisher ->
                    System.out.printf(">>> EVENT: A new publisher has entered the room: %s (ID: %d)%n",
                        publisher.display(), publisher.id()));
            }

            @Override
            public void onUnpublished(UnpublishedEvent event) {
                System.out.println(">>> EVENT: Publisher " + event.unpublished() + " has unpublished their stream.");
            }

            @Override
            public void onParticipantLeft(ParticipantLeftEvent event) {
                 System.out.println(">>> EVENT: Participant " + event.leaving() + " has left the room.");
            }

            @Override
            public void onRoomDestroyed(RoomDestroyedEvent event) {
                System.out.println(">>> EVENT: Room " + event.room() + " has been destroyed.");
            }
        });

        // 3. Create a new room
        System.out.println("Creating a new video room...");
        CreateRoomRequest createRequest = new CreateRoomRequest.Builder()
            .setDescription("My Java SDK Test Room")
            .setPublishers(6)
            .build();
        CreateRoomResponse createResponse = videoRoomHandle.createRoom(createRequest).get();
        final long roomId = createResponse.room();
        System.out.println("Room created with ID: " + roomId);
        Thread.sleep(500); // Pause for clarity

        // 4. Join the room as a publisher
        System.out.println("Joining room " + roomId + " as a publisher......");
        JoinRoomRequest joinRequest = new JoinRoomRequest.Builder(roomId)
            .setDisplay("JavaSDKUser")
            .build();
        videoRoomHandle.join(joinRequest).get(); // join() is async, the onJoined event will fire

        // Give Janus time to process and send the 'joined' event
        Thread.sleep(1000);

        // 5. List participants (should include us)
        System.out.println("Listing participants in room " + roomId + "...");
        ListParticipantsRequest listRequest = new ListParticipantsRequest(roomId);
        ListParticipantsResponse listResponse = videoRoomHandle.listParticipants(listRequest).get();
        System.out.println("Participants found: " + listResponse.participants().size());
        listResponse.participants().forEach(p ->
            System.out.printf("  - Participant ID: %d, Display: '%s', Publisher: %b%n",
                p.id(), p.display(), p.publisher()));
        Thread.sleep(500);

        // 6. Conceptually "publish" a stream.
        // In a real application, this would involve sending a JSEP offer from a WebRTC client.
        // The handle's `sendMessage(body, jsep)` would be used. For this example, we just
        // simulate the action and then unpublish.
        System.out.println("Simulating publishing a stream... (In a real app, this sends a JSEP offer)");
        // An `onPublisherAdded` event would be sent to all participants after this.
        Thread.sleep(1000);

        // 7. Unpublish the stream
        System.out.println("Unpublishing the stream...");
        // In this example, we comment out the unpublish call.
        // The Janus plugin would return an error "Can't unpublish, not published"
        // because we never sent a real `publish` request with a JSEP offer.
        // videoRoomHandle.unpublish().get();
        Thread.sleep(500);

        // 8. Leave the room
        System.out.println("Leaving the room...");
        videoRoomHandle.leave().get(); // An `onParticipantLeft` event will fire for others
        Thread.sleep(500);

        // 9. Destroy the room
        System.out.println("Destroying room " + roomId + "...");
        DestroyRoomRequest destroyRequest = new DestroyRoomRequest(roomId, null, true);
        videoRoomHandle.destroyRoom(destroyRequest).get(); // An `onRoomDestroyed` event will fire for the handle

        System.out.println("\n--- VideoRoom Example Finished ---\n");
    }

    public static void runAdminClientExample(JanusAdminConfiguration adminConfig) throws Exception {
        System.out.println("\n--- Running Admin Client Example ---\n");

        JanusAdminClient adminClient = new JanusAdminClient(adminConfig);

        adminClient.getAdminMonitor().addListener(event -> {
            System.out.println(">>> ADMIN EVENT: " + event.toString(2));
        });

        System.out.println("Pinging admin endpoint...");
        adminClient.ping().thenAccept(response -> {
            System.out.println("Ping response: " + response.toString(2));
        }).get();

        System.out.println("Getting server info...");
        adminClient.info().thenAccept(info -> {
            System.out.println("Server info: " + info.versionString());
        }).get();

        System.out.println("Getting status...");
        adminClient.getStatus().thenAccept(status -> {
            System.out.println("Status: " + status.toString(2));
        }).get();

        System.out.println("Listing active sessions...");
        ListSessionsResponse sessionsResponse = adminClient.listSessions().get();
        System.out.println("Active sessions: " + sessionsResponse.getSessionIds());

        if (!sessionsResponse.getSessionIds().isEmpty()) {
            long firstSessionId = sessionsResponse.getSessionIds().get(0);
            System.out.println("Listing handles for session: " + firstSessionId);
            adminClient.listHandles(firstSessionId).thenAccept(handles -> {
                System.out.println("Handles: " + handles.getHandleIds());
            }).get();
        }

        System.out.println("Setting log level to 4...");
        adminClient.setLogLevel(4).get();
        System.out.println("Log level set.");

        System.out.println("Getting status again...");
        adminClient.getStatus().thenAccept(status -> {
            System.out.println("Status: " + status.toString(2));
        }).get();

        System.out.println("Setting log level back to 3...");
        adminClient.setLogLevel(3).get();
        System.out.println("Log level set.");


        adminClient.disconnect();
        System.out.println("\n--- Admin Client Example Finished ---\n");
    }
}