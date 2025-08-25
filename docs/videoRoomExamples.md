# Janus VideoRoom Plugin: Examples and Usage

This document provides a comprehensive guide with detailed examples on how to use the `VideoRoomHandle` in the Java SDK to interact with the Janus VideoRoom plugin.

## 1. Introduction

The VideoRoom plugin implements a Selective Forwarding Unit (SFU) for creating multi-party video conferences. Participants can join virtual "rooms", publish their own media streams (audio/video), and subscribe to the streams of other participants.

Our Java SDK provides a `VideoRoomHandle` that abstracts the underlying Janus API, allowing you to interact with video rooms in a type-safe and object-oriented way.

## 2. Setup and Initialization

First, you need a `JanusClient` instance connected to your Janus Gateway and a `JanusSession`. From the session, you can attach to the VideoRoom plugin to get a `VideoRoomHandle`.

```java
// Assuming you have a JanusClient and JanusSession already created
// JanusClient client = new JanusClient(new JanusConfiguration("ws://your-janus-instance:8188"));
// JanusSession session = client.createSession().join();

try {
    // Attach to the VideoRoom plugin
    VideoRoomHandle videoRoomHandle = session.attachToVideoRoom().join();
    System.out.println("Successfully attached to VideoRoom plugin with handle ID: " + videoRoomHandle.getHandleId());

    // Now you can use the videoRoomHandle...

} catch (Exception e) {
    System.err.println("Error attaching to VideoRoom plugin: " + e.getMessage());
    e.printStackTrace();
}
```

## 3. Implementing the `JanusVideoRoomListener`

The VideoRoom is highly asynchronous. Most state changes (participants joining, leaving, new streams) are communicated through events. To receive these, you must implement the `JanusVideoRoomListener` interface and add it to your handle.

```java
import io.github.kinsleykajiva.janus.plugins.videoroom.events.*;
import io.github.kinsleykajiva.janus.plugins.videoroom.listeners.JanusVideoRoomListener;
import org.json.JSONObject;

public class MyVideoRoomListener implements JanusVideoRoomListener {

    @Override
    public void onJoined(JoinedEvent event) {
        System.out.println("Successfully joined room " + event.room());
        System.out.println("My ID is: " + event.id());
        // You are now in the room. If others are already publishing, you'll find them in event.publishers()
        event.publishers().forEach(publisher -> {
            System.out.println("  - Active publisher: " + publisher.display() + " (ID: " + publisher.id() + ")");
            // Here you would typically create a new subscriber handle to subscribe to this publisher
        });
    }

    @Override
    public void onPublisherAdded(PublisherAddedEvent event) {
        // A new publisher has joined the room.
        event.publishers().forEach(publisher -> {
            System.out.println("A new publisher has joined the room: " + publisher.display() + " (ID: " + publisher.id() + ")");
            // Time to subscribe to their stream!
        });
    }

    @Override
    public void onUnpublished(UnpublishedEvent event) {
        System.out.println("Publisher " + event.unpublished() + " has unpublished their stream.");
        // Clean up any UI elements or subscriber handles related to this publisher.
    }

    @Override
    public void onParticipantLeft(ParticipantLeftEvent event) {
        System.out.println("Participant " + event.leaving() + " has left the room.");
    }

    @Override
    public void onEvent(JSONObject event) {
        // A catch-all for any other events, useful for debugging.
        System.out.println("Received a generic VideoRoom event: " + event.toString(2));
    }

    // Implement other event handlers as needed...
}

// In your main logic:
MyVideoRoomListener listener = new MyVideoRoomListener();
videoRoomHandle.addVideoRoomListener(listener);
```

## 4. Room Management

### Creating a Room

You can create rooms dynamically.

```java
// Create a request object using the builder pattern
CreateRoomRequest createRequest = new CreateRoomRequest.Builder()
    .setDescription("My Awesome Test Room")
    .setPublishers(6) // Max 6 concurrent publishers
    .setBitrate(512000) // 512 kbit/s bitrate cap
    .setPermanent(false) // This room will be destroyed when Janus restarts
    .build();

// Send the request and wait for the response
try {
    CreateRoomResponse createResponse = videoRoomHandle.createRoom(createRequest).join();
    long newRoomId = createResponse.room();
    System.out.println("Successfully created room with ID: " + newRoomId);
} catch (Exception e) {
    System.err.println("Error creating room: " + e.getMessage());
}
```

### Listing Rooms

```java
try {
    ListRoomsResponse listResponse = videoRoomHandle.listRooms().join();
    System.out.println("Available rooms:");
    listResponse.list().forEach(room -> {
        System.out.println("  - Room " + room.room() + ": " + room.description());
    });
} catch (Exception e) {
    System.err.println("Error listing rooms: " + e.getMessage());
}
```

## 5. Publisher Workflow

A typical publisher joins a room and then publishes their media.

### Joining a Room

```java
long roomId = 1234; // The ID of the room you want to join

// Use a builder to create the join request
JoinRoomRequest joinRequest = new JoinRoomRequest.Builder(roomId)
    .setDisplay("JulesTheEngineer") // Set a display name
    .build();

// Send the asynchronous request. The result will come as an `onJoined` event.
videoRoomHandle.join(joinRequest).join();
System.out.println("Join request sent. Waiting for 'joined' event...");
```

### Publishing Media

After joining, you can publish your stream. This request must be accompanied by a JSEP SDP Offer from your WebRTC peer connection. The `JanusHandle` provides a method to send a message with a JSEP payload.

```java
// This is a conceptual example. The actual SDP offer would come from your WebRTC client.
String sdpOffer = "v=0\r\no=- 5244...";
JSONObject jsep = new JSONObject().put("type", "offer").put("sdp", sdpOffer);

// Create the publish request body
JSONObject body = new PublishRequest.Builder()
    .setAudioCodec("opus")
    .setVideoCodec("vp8")
    .build()
    .toJson();

// The response will contain the JSEP answer
CompletableFuture<JSONObject> publishResponseFuture = videoRoomHandle.sendMessage(body, jsep);
publishResponseFuture.thenAccept(answer -> {
    System.out.println("Received JSEP answer: " + answer.toString(2));
    // The "configured": "ok" event is inside this response's plugindata.
    // A `publisher-added` event will be sent separately to all participants.
});
```

## 6. Subscriber Workflow

A subscriber handle is separate from a publisher handle. Typically, for each publisher you want to view, you create a new subscriber handle.

```java
// Let's assume we learned about a publisher with ID `5678` from an event.
long publisherIdToSubscribeTo = 5678;
long roomId = 1234;

// 1. Create a NEW handle for this subscription
VideoRoomHandle subscriberHandle = session.attachToVideoRoom().join();

// 2. Create a list of streams to subscribe to.
// Here we subscribe to the publisher's stream with mid "0"
List<Subscription> subscriptions = List.of(new Subscription(publisherIdToSubscribeTo, "0", null));

// 3. Create and send the subscribe request.
SubscribeRequest subscribeRequest = new SubscribeRequest.Builder(roomId, subscriptions).build();

// 4. Send the request. The response will be an `attached` event with a JSEP offer.
subscriberHandle.subscribe(subscribeRequest).join();
// Note: You need a listener on the `subscriberHandle` to catch the `onSubscriberAttached` event,
// which will contain the JSEP offer from the plugin.

// 5. Once you get the JSEP offer, you generate an answer and send it back with a `start` request.
JSONObject startBody = new StartSubscriptionRequest().toJson();
JSONObject jsepAnswer = new JSONObject().put("type", "answer").put("sdp", "...");
subscriberHandle.sendMessage(startBody, jsepAnswer);

System.out.println("Subscription started. Media should now be flowing.");
```

## 7. Advanced Features

### Kicking a Participant

```java
// You may need the room secret for this, depending on room configuration.
String roomSecret = "supersecret";
long roomId = 1234;
long participantToKickId = 9999;

KickRequest kickRequest = new KickRequest(roomId, participantToKickId, roomSecret);
try {
    videoRoomHandle.kick(kickRequest).join();
    System.out.println("Successfully kicked participant " + participantToKickId);
} catch (Exception e) {
    System.err.println("Failed to kick participant: " + e.getMessage());
}
```

### Switching Streams

You can switch a subscriber's stream to a different publisher without a full renegotiation.

```java
// Assume `subscriberHandle` is already subscribed to publisher A.
// Now we want to switch it to publisher B (ID 4321, mid "0")
long newPublisherId = 4321;
String newPublisherMid = "0";
String subscriberMidToUpdate = "0"; // The mid of our subscription we want to change

SwitchStream switchStream = new SwitchStream(newPublisherId, newPublisherMid, subscriberMidToUpdate);
SwitchRequest switchRequest = new SwitchRequest(List.of(switchStream));

// A `switched` event will be sent upon success
subscriberHandle.switchRequest(switchRequest).join();
```

## 8. Complete `Main.java` Example

Here is the full, runnable example from `Main.java` that demonstrates a typical publisher workflow. This code can be used as a starting point for building your own application.

```java
package io.github.kinsleykajiva;

import io.github.kinsleykajiva.janus.JanusClient;
import io.github.kinsleykajiva.janus.JanusConfiguration;
import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.ServerInfo;
import io.github.kinsleykajiva.janus.handle.impl.VideoRoomHandle;
import io.github.kinsleykajiva.janus.plugins.videoroom.events.*;
import io.github.kinsleykajiva.janus.plugins.videoroom.listeners.JanusVideoRoomListener;
import io.github.kinsleykajiva.janus.plugins.videoroom.models.*;
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

            // Run the VideoRoom example
            runVideoRoomExample(session);


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
        System.out.println("Joining room " + roomId + " as a publisher...");
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
}
```
