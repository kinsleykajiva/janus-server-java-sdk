# Janus SDK: AudioBridge Plugin Documentation

The AudioBridge plugin provides a powerful API for creating and managing audio conferencing rooms. This document serves as a guide for developers using the Janus SDK to integrate audio bridging functionalities into their applications. It covers the core concepts, API usage, event handling, and provides practical examples.

## Core Concepts

The primary interaction with the AudioBridge plugin is through the `AudioBridgeHandle`. This handle is associated with a specific user and allows you to perform actions within the audio conferencing system, such as creating rooms, joining them, and managing participants.

Events from the plugin, such as a user joining or leaving a room, are delivered asynchronously. To receive these events, you must implement the `JanusAudioBridgeListener` and register it with your `AudioBridgeHandle`.

## Getting Started: Obtaining an AudioBridgeHandle

Before you can interact with the AudioBridge plugin, you need a Janus session and a handle attached to that session. The following example demonstrates how to create a `JanusClient`, establish a session, and then create an `AudioBridgeHandle`.

```java
import io.github.kinsleykajiva.janus.client.JanusClient;
import io.github.kinsleykajiva.janus.client.JanusSession;
import io.github.kinsleykajiva.janus.client.handle.impl.AudioBridgeHandle;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class AudioBridgeExample {
	
	public static void main(String[] args) throws Exception {
		// 1. Create a JanusClient
		// Replace with your Janus server's WebSocket URI
		URI serverUri = new URI("ws://your-janus-server:8188/");
		JanusClient janusClient = new JanusClient(serverUri);
		janusClient.connect();
		
		// 2. Create a JanusSession
		CompletableFuture<JanusSession> sessionFuture = janusClient.createSession();
		JanusSession session = sessionFuture.get();
		
		// 3. Create an AudioBridgeHandle
		CompletableFuture<AudioBridgeHandle> handleFuture = session.attachToAudioBridge();
		AudioBridgeHandle audioBridgeHandle = handleFuture.get();
		
		System.out.println("AudioBridgeHandle created with ID: " + audioBridgeHandle.getHandleId());
		
		// You can now use the audioBridgeHandle to interact with the plugin
		// ...
		
		// Clean up when done
		janusClient.disconnect();
	}
}
```

**Key Steps:**

1.  **`JanusClient`**: This is the main entry point to the Janus server. You instantiate it with the WebSocket URI of your Janus instance.
2.  **`JanusSession`**: A session represents a logical connection to the Janus server. You create it from the `JanusClient`.
3.  **`AudioBridgeHandle`**: Once you have a session, you can "attach" a handle to a specific plugin. The `session.attachToAudioBridge()` method is a convenient way to get a handle specifically for the AudioBridge plugin.

With the `audioBridgeHandle` instance, you are now ready to send commands and listen for events. The following sections will detail the available methods and events.

## API Reference: `AudioBridgeHandle`

The `AudioBridgeHandle` class is your primary tool for interacting with the AudioBridge plugin. All methods that send a request to the Janus server return a `CompletableFuture`, allowing for non-blocking, asynchronous operations.

### Room Management

#### `createRoom(CreateRoomRequest request)`

Creates a new audio conference room.

-   **Parameters:**
    -   `request`: A `CreateRoomRequest` object. Use this to specify properties for the new room, such as its description, PIN, and whether it's private.
-   **Returns:** `CompletableFuture<AudioBridgeRoom>` - A future that completes with an `AudioBridgeRoom` object, representing the newly created room.
-   **Example:**
    ```java
import io.github.kinsleykajiva.janus.client.handle.impl.AudioBridgeHandle;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.models.CreateRoomRequest;
......

    CreateRoomRequest.Builder builder =new CreateRoomRequest.Builder().
					                                   
					                                   setRoom(System.currentTimeMillis())
					                                   .setIsPrivate(false).setPermanent(false)
					                                   .setPin("1234")
					                                   .setDescription("Random Room")
					;
			CreateRoomRequest createRoomRequest = builder.build();
			
			var room=audioBridgeHandle.createRoom(createRoomRequest).get();
			logger.info("Created roomId with ID: {}", room.room());
    ```

#### `destroyRoom(DestroyRoomRequest request)`

Destroys an existing audio room.

-   **Parameters:**
    -   `request`: A `DestroyRoomRequest` object specifying the `roomId` of the room to destroy and, optionally, a `secret` if required by the room's configuration.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the room has been successfully destroyed.
-   **Example:**
    ```java
    long roomIdToDestroy = 123456789; // The ID of the room to destroy
    DestroyRoomRequest destroyRequest = new DestroyRoomRequest(roomIdToDestroy);

    audioBridgeHandle.destroyRoom(destroyRequest)
        .thenRun(() -> {
            System.out.println("Room " + roomIdToDestroy + " destroyed successfully.");
        })
        .exceptionally(ex -> {
            System.err.println("Failed to destroy room: " + ex.getMessage());
            return null;
        });
    ```

#### `listRooms()`

Retrieves a list of all available (non-private) AudioBridge rooms.

-   **Returns:** `CompletableFuture<List<AudioBridgeRoom>>` - A future that completes with a list of `AudioBridgeRoom` objects.
-   **Example:**
    ```java
    audioBridgeHandle.listRooms()
        .thenAccept(rooms -> {
            System.out.println("Available rooms:");
            rooms.forEach(room -> {
                System.out.println("- " + room.getDescription() + " (ID: " + room.getRoomId() + ")");
            });
        });
    ```

#### `editRoom(EditRoomRequest request)`

Edits the properties of an existing room (e.g., changing the description, PIN, or secret).

-   **Parameters:**
    -   `request`: An `EditRoomRequest` object containing the `roomId` and the new properties to set.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the room has been successfully edited.
-   **Example:**
    ```java
    long roomToEdit = 123456789;
    EditRoomRequest editRequest = new EditRoomRequest(roomToEdit);
    editRequest.setNewDescription("My updated conference room");
    editRequest.setNewPin("5678");

    audioBridgeHandle.editRoom(editRequest)
        .thenRun(() -> {
            System.out.println("Room " + roomToEdit + " edited successfully.");
        })
        .exceptionally(ex -> {
            System.err.println("Failed to edit room: " + ex.getMessage());
            return null;
        });
    ```

#### `exists(ExistsRequest request)`

Checks if a specific room exists.

-   **Parameters:**
    -   `request`: An `ExistsRequest` object containing the `roomId` to check.
-   **Returns:** `CompletableFuture<ExistsResponse>` - A future that completes with an `ExistsResponse` object, which has a `boolean exists()` method.
-   **Example:**
    ```java
    long roomToCheck = 123456789;
    ExistsRequest existsRequest = new ExistsRequest(roomToCheck);

    audioBridgeHandle.exists(existsRequest)
        .thenAccept(response -> {
            if (response.exists()) {
                System.out.println("Room " + roomToCheck + " exists.");
            } else {
                System.out.println("Room " + roomToCheck + " does not exist.");
            }
        });
    ```

### Participant Management

#### `joinRoom(JoinRoomRequest request)`

Sends a request for the local user to join a room. This is an asynchronous operation. A successful join is not confirmed by the return of this future, but by an `onJoined` event delivered to registered `JanusAudioBridgeListener`s.

-   **Parameters:**
    -   `request`: A `JoinRoomRequest` object specifying the `roomId` to join. You can also set properties like `pin`, `displayName`, and whether the user should join muted.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the join request has been acknowledged by the server. It does **not** signify that the join is complete.
-   **Example:**
    ```java
    long roomToJoin = 123456789;
    JoinRoomRequest joinRequest = new JoinRoomRequest(roomToJoin);
    joinRequest.setDisplayName("John Doe");
    joinRequest.setMuted(true); // Join the room already muted

    audioBridgeHandle.joinRoom(joinRequest)
        .thenRun(() -> {
            System.out.println("Join request sent. Waiting for 'onJoined' event...");
        })
        .exceptionally(ex -> {
            System.err.println("Failed to send join request: " + ex.getMessage());
            return null;
        });
    ```

#### `leave()`

Sends a request for the local user to leave the current room. A successful departure will be indicated by an `onParticipantLeft` event with your own ID.

-   **Returns:** `CompletableFuture<Void>` - A future that completes when the leave request is acknowledged by the server.
-   **Example:**
    ```java
    audioBridgeHandle.leave()
        .thenRun(() -> {
            System.out.println("Leave request sent.");
        });
    ```

#### `configure(ConfigureRequest request)`

Configures the local user's participation in the room (e.g., muting/unmuting or changing the display name).

-   **Parameters:**
    -   `request`: A `ConfigureRequest` object. You can specify `muted` to mute/unmute or `display` to change the display name.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the configure request is acknowledged.
-   **Example:**
    ```java
    // Mute the local user
    ConfigureRequest muteRequest = new ConfigureRequest();
    muteRequest.setMuted(true);

    audioBridgeHandle.configure(muteRequest)
        .thenRun(() -> {
            System.out.println("Request to mute has been sent.");
        });

    // Change display name
    ConfigureRequest nameChangeRequest = new ConfigureRequest();
    nameChangeRequest.setDisplay("John D. Updated");

    audioBridgeHandle.configure(nameChangeRequest);
    ```

#### `listParticipants(long roomId)`

Retrieves the list of participants currently in a specific room.

-   **Parameters:**
    -   `roomId`: The unique ID of the room.
-   **Returns:** `CompletableFuture<List<AudioBridgeParticipant>>` - A future that completes with a list of `AudioBridgeParticipant` objects.
-   **Example:**
    ```java
    long roomToList = 123456789;
    audioBridgeHandle.listParticipants(roomToList)
        .thenAccept(participants -> {
            System.out.println("Participants in room " + roomToList + ":");
            participants.forEach(p -> {
                System.out.println(
                    "- ID: " + p.getId() +
                    ", Name: " + p.getDisplayName() +
                    ", Muted: " + p.isMuted()
                );
            });
        });
    ```

### Moderation

These methods are typically used by moderators to manage participants in a room.

#### `kick(KickRequest request)`

Kicks a specific participant from a room.

-   **Parameters:**
    -   `request`: A `KickRequest` object specifying the `roomId` and the `participantId` to kick.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the kick request is acknowledged.
-   **Example:**
    ```java
    long roomId = 123456789;
    long participantToKick = 987654321;
    KickRequest kickRequest = new KickRequest(roomId, participantToKick);

    audioBridgeHandle.kick(kickRequest)
        .thenRun(() -> System.out.println("Kick request sent for participant " + participantToKick));
    ```

#### `mute(MuteParticipantRequest request)`

Mutes a specific participant in the room.

-   **Parameters:**
    -   `request`: A `MuteParticipantRequest` object specifying the `roomId` and the `participantId` to mute.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the mute request is acknowledged.
-   **Example:**
    ```java
    long roomId = 123456789;
    long participantToMute = 987654321;
    MuteParticipantRequest muteRequest = new MuteParticipantRequest(roomId, participantToMute);

    audioBridgeHandle.mute(muteRequest)
        .thenRun(() -> System.out.println("Mute request sent for participant " + participantToMute));
    ```

#### `unmute(UnmuteParticipantRequest request)`

Unmutes a specific participant in the room.

-   **Parameters:**
    -   `request`: An `UnmuteParticipantRequest` object specifying the `roomId` and the `participantId` to unmute.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the unmute request is acknowledged.
-   **Example:**
    ```java
    long roomId = 123456789;
    long participantToUnmute = 987654321;
    UnmuteParticipantRequest unmuteRequest = new UnmuteParticipantRequest(roomId, participantToUnmute);

    audioBridgeHandle.unmute(unmuteRequest)
        .thenRun(() -> System.out.println("Unmute request sent for participant " + participantToUnmute));
    ```

#### `muteRoom(MuteRoomRequest request)`

Mutes all participants in the room.

-   **Parameters:**
    -   `request`: A `MuteRoomRequest` object specifying the `roomId`.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the mute request is acknowledged.
-   **Example:**
    ```java
    long roomId = 123456789;
    MuteRoomRequest muteRoomRequest = new MuteRoomRequest(roomId);

    audioBridgeHandle.muteRoom(muteRoomRequest)
        .thenRun(() -> System.out.println("Mute request sent for room " + roomId));
    ```

#### `unmuteRoom(UnmuteRoomRequest request)`

Unmutes all participants in the room.

-   **Parameters:**
    -   `request`: An `UnmuteRoomRequest` object specifying the `roomId`.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the unmute request is acknowledged.
-   **Example:**
    ```java
    long roomId = 123456789;
    UnmuteRoomRequest unmuteRoomRequest = new UnmuteRoomRequest(roomId);

    audioBridgeHandle.unmuteRoom(unmuteRoomRequest)
        .thenRun(() -> System.out.println("Unmute request sent for room " + roomId));
    ```

### Advanced Operations

#### `rtpForward(RtpForwardRequest request)`

Forwards the audio stream of a participant to a remote RTP server.

-   **Parameters:**
    -   `request`: An `RtpForwardRequest` object containing details like `roomId`, `participantId`, `host`, `port`, etc.
-   **Returns:** `CompletableFuture<RtpForwardResponse>` - A future that completes with an `RtpForwardResponse`, which includes the `rtp_stream` details.
-   **Example:**
    ```java
    long roomId = 123456789;
    long participantId = 987654321;
    RtpForwardRequest rtpRequest = new RtpForwardRequest(roomId, participantId, "192.168.1.100", 5004);

    audioBridgeHandle.rtpForward(rtpRequest)
        .thenAccept(response -> {
            System.out.println("RTP forwarding started for stream: " + response.getRtpStream().getStreamId());
        });
    ```

#### `stopRtpForward(StopRtpForwardRequest request)`

Stops an existing RTP forward.

-   **Parameters:**
    -   `request`: A `StopRtpForwardRequest` object specifying the `roomId` and the `streamId` to stop.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the forwarding is stopped.
-   **Example:**
    ```java
    long roomId = 123456789;
    long streamId = 1122334455; // The ID of the stream from RtpForwardResponse
    StopRtpForwardRequest stopRtpRequest = new StopRtpForwardRequest(roomId, streamId);

    audioBridgeHandle.stopRtpForward(stopRtpRequest)
        .thenRun(() -> System.out.println("RTP forwarding stopped for stream " + streamId));
    ```

#### `playFile(PlayFileRequest request)`

Plays a pre-recorded file into the conference.

-   **Parameters:**
    -   `request`: A `PlayFileRequest` object specifying the `roomId` and the path to the file to play.
-   **Returns:** `CompletableFuture<PlayFileResponse>` - A future that completes with a `PlayFileResponse`, containing the `announcement_id`.
-   **Example:**
    ```java
    long roomId = 123456789;
    PlayFileRequest playRequest = new PlayFileRequest(roomId, "/path/to/your/audio.opus");

    audioBridgeHandle.playFile(playRequest)
        .thenAccept(response -> {
            System.out.println("Playing file with announcement ID: " + response.getAnnouncementId());
        });
    ```

#### `stopFile(StopFileRequest request)`

Stops a file that is currently being played.

-   **Parameters:**
    -   `request`: A `StopFileRequest` object with the `roomId` and the `announcementId` to stop.
-   **Returns:** `CompletableFuture<StopFileResponse>` - A future that completes when the file playback is stopped.
-   **Example:**
    ```java
    long roomId = 123456789;
    long announcementId = 556677889; // from PlayFileResponse
    StopFileRequest stopRequest = new StopFileRequest(roomId, announcementId);

    audioBridgeHandle.stopFile(stopRequest)
        .thenRun(() -> System.out.println("Stopped playing file."));
    ```

#### `enableRecording(EnableRecordingRequest request)`

Enables or disables recording for a specific participant.

-   **Parameters:**
    -   `request`: An `EnableRecordingRequest` object specifying the `roomId`, `participantId`, and a boolean `record` flag. You can also specify the output `file` path.
-   **Returns:** `CompletableFuture<Void>` - A future that completes when the recording state is changed.
-   **Example:**
    ```java
    long roomId = 123456789;
    long participantId = 987654321;
    EnableRecordingRequest recordRequest = new EnableRecordingRequest(
        roomId,
        participantId,
        true, // Start recording
        "/path/to/recordings/user-987654321.opus"
    );

    audioBridgeHandle.enableRecording(recordRequest)
        .thenRun(() -> System.out.println("Recording enabled for participant " + participantId));
    ```

## Event Handling: `JanusAudioBridgeListener`

To react to events happening in the audio conference, you need to implement the `JanusAudioBridgeListener` interface and register it with your `AudioBridgeHandle`. This is crucial for building an interactive application, as it's how you get notified about participants joining, leaving, being muted, and other state changes.

### Adding and Removing a Listener

You can add and remove listeners using the following methods on your `AudioBridgeHandle`:

-   `addAudioBridgeListener(JanusAudioBridgeListener listener)`
-   `removeAudioBridgeListener(JanusAudioBridgeListener listener)`

**Example:**
```java
// Create an instance of your listener implementation
MyAudioBridgeListener myListener = new MyAudioBridgeListener();

// Add the listener to the handle
audioBridgeHandle.addAudioBridgeListener(myListener);

// When you're done, you can remove it
// audioBridgeHandle.removeAudioBridgeListener(myListener);
```

### Listener Methods

Here are the methods you can implement from the `JanusAudioBridgeListener` interface.

#### `onJoined(JoinedEvent event)`

Called when the **local user** has successfully joined a room. This is the confirmation that your `joinRoom` request was successful.

-   **Event Object:** `JoinedEvent`
    -   `getRoomId()`: The ID of the room that was joined.
    -   `getDescription()`: The description of the room.
    -   `getParticipants()`: A list of `AudioBridgeParticipant` objects for other users already in the room.

#### `onParticipantJoined(ParticipantJoinedEvent event)`

Called when a **remote participant** joins the room you are in.

-   **Event Object:** `ParticipantJoinedEvent`
    -   `getRoomId()`: The ID of the room.
    -   `getParticipant()`: An `AudioBridgeParticipant` object for the new participant.

#### `onParticipantLeft(ParticipantLeftEvent event)`

Called when any participant (including the local user) leaves the room.

-   **Event Object:** `ParticipantLeftEvent`
    -   `getRoomId()`: The ID of the room.
    -   `getLeavingParticipantId()`: The unique ID of the participant who left.

#### `onParticipantUpdated(ParticipantUpdatedEvent event)`

Called when a participant's state is updated (e.g., they are muted, unmuted, or their display name changes).

-   **Event Object:** `ParticipantUpdatedEvent`
    -   `getRoomId()`: The ID of the room.
    -   `getParticipant()`: The `AudioBridgeParticipant` object with the updated details.

#### `onRoomDestroyed(RoomDestroyedEvent event)`

Called when the room you are in is destroyed.

-   **Event Object:** `RoomDestroyedEvent`
    -   `getRoomId()`: The ID of the room that was destroyed.

#### `onEvent(JSONObject event)`

A generic, catch-all callback for any event from the plugin. This is useful for debugging or handling custom events not covered by the specific methods above.

-   **Event Object:** `JSONObject` - The raw JSON data of the event from Janus.

### Example Listener Implementation

```java
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.events.*;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.listeners.JanusAudioBridgeListener;
import org.json.JSONObject;

public class MyAudioBridgeListener implements JanusAudioBridgeListener {
	
	@Override
	public void onJoined(JoinedEvent event) {
		System.out.println("Successfully joined room: " + event.getRoomId());
		System.out.println("Room description: " + event.getDescription());
		event.getParticipants().forEach(p -> {
			System.out.println("- Existing participant: " + p.getDisplayName());
		});
	}
	
	@Override
	public void onParticipantJoined(ParticipantJoinedEvent event) {
		System.out.println(
				"Participant joined: " + event.getParticipant().getDisplayName() +
						" (ID: " + event.getParticipant().getId() + ")"
		);
	}
	
	@Override
	public void onParticipantLeft(ParticipantLeftEvent event) {
		System.out.println("Participant left: " + event.getLeavingParticipantId());
	}
	
	@Override
	public void onParticipantUpdated(ParticipantUpdatedEvent event) {
		System.out.println(
				"Participant updated: " + event.getParticipant().getDisplayName() +
						" | Muted: " + event.getParticipant().isMuted()
		);
	}
	
	@Override
	public void onRoomDestroyed(RoomDestroyedEvent event) {
		System.out.println("The room has been destroyed: " + event.getRoomId());
	}
	
	@Override
	public void onEvent(JSONObject event) {
		// Optional: Log all raw events for debugging
		// System.out.println("Received raw event: " + event.toString(2));
	}
}
```

## Data Models

The SDK uses a variety of model classes to represent requests and responses for the AudioBridge plugin. These classes, found in the `io.github.kinsleykajiva.janus.client.plugins.audiobridge.models` package, provide a type-safe way to construct requests and parse responses.

Below is a brief overview of some of the key model classes.

### Request Models

-   **`CreateRoomRequest`**: Used to define the properties of a new room (description, PIN, secret, etc.) when calling `createRoom`.
-   **`DestroyRoomRequest`**: Used to specify the `roomId` to be destroyed.
-   **`JoinRoomRequest`**: Used to specify the `roomId` to join, and optionally, the user's display name, PIN, and initial muted state.
-   **`ConfigureRequest`**: Used to change the local user's state in the room, such as muting/unmuting or changing the display name.
-   **`KickRequest`**: Used to kick a participant from a room, requiring a `roomId` and `participantId`.
-   **`MuteParticipantRequest`**: Used to mute a specific participant, requiring a `roomId` and `participantId`.
-   **`RtpForwardRequest`**: Used to provide all the necessary details for starting an RTP forward, including host, port, and stream details.
-   **`PlayFileRequest`**: Used to specify the `roomId` and the `file` to be played as an announcement.

### Response and Entity Models

-   **`AudioBridgeRoom`**: Represents an audio conference room, containing its ID, description, and other properties. Returned by `createRoom` and `listRooms`.
-   **`AudioBridgeParticipant`**: Represents a participant in a room, containing their ID, display name, and muted status.
-   **`ExistsResponse`**: A simple response object from the `exists` request, containing a boolean `exists()` method.
-   **`RtpForwardResponse`**: Contains the details of a newly created RTP stream after a successful `rtpForward` request.

By using these models, you can avoid constructing JSON objects manually and benefit from the compile-time checks and auto-completion provided by your IDE.

## Full Example: A Simple Conference Bot

This example demonstrates a simple "bot" that connects to Janus, creates a room, joins it, waits for a short period, and then leaves and destroys the room. It showcases how to use the `AudioBridgeHandle` and `JanusAudioBridgeListener` together.

```java
import io.github.kinsleykajiva.janus.client.JanusClient;
import io.github.kinsleykajiva.janus.client.JanusSession;
io.github.kinsleykajiva.janus.client.handle.impl.AudioBridgeHandle;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.events.*;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.listeners.JanusAudioBridgeListener;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.models.CreateRoomRequest;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.models.DestroyRoomRequest;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.models.JoinRoomRequest;
import org.json.JSONObject;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConferenceBotExample {
	
	// Use a latch to keep the application running until the bot is done
	private static CountDownLatch latch = new CountDownLatch(1);
	private static long myRoomId;
	
	public static void main(String[] args) throws Exception {
		URI serverUri = new URI("ws://your-janus-server:8188/");
		io.github.kinsleykajiva.janus.client.JanusClient janusClient = new JanusClient(serverUri);
		janusClient.connect();
		
		JanusSession session = janusClient.createSession().get();
		AudioBridgeHandle audioBridgeHandle = session.attachToAudioBridge().get();
		
		// Add a listener to react to events
		audioBridgeHandle.addAudioBridgeListener(new ConferenceListener());
		
		// --- Workflow ---
		
		// 1. Create a room
		CreateRoomRequest createRequest = new CreateRoomRequest("Bot's Conference Room");
		audioBridgeHandle.createRoom(createRequest)
				.thenCompose(room -> {
					myRoomId = room.getRoomId();
					System.out.println("Room created: " + myRoomId);
					
					// 2. Join the room
					JoinRoomRequest joinRequest = new JoinRoomRequest(myRoomId);
					joinRequest.setDisplayName("ConferenceBot");
					return audioBridgeHandle.joinRoom(joinRequest);
				})
				.thenRun(() -> {
					System.out.println("Join request sent. The bot will leave in 15 seconds.");
				})
				.exceptionally(ex -> {
					System.err.println("Error in setup: " + ex.getMessage());
					latch.countDown(); // Release latch on error
					return null;
				});
		
		// Keep the main thread alive
		latch.await(30, TimeUnit.SECONDS);
		
		// --- Cleanup ---
		System.out.println("Cleaning up...");
		
		// 4. Leave the room (optional, as destroying the room kicks everyone out)
		audioBridgeHandle.leave().get();
		
		// 5. Destroy the room
		DestroyRoomRequest destroyRequest = new DestroyRoomRequest(myRoomId);
		audioBridgeHandle.destroyRoom(destroyRequest).get();
		System.out.println("Room destroyed.");
		
		janusClient.disconnect();
		System.out.println("Disconnected.");
	}
	
	// A simple listener implementation for our bot
	static class ConferenceListener implements JanusAudioBridgeListener {
		@Override
		public void onJoined(JoinedEvent event) {
			System.out.println("Bot successfully joined room " + event.getRoomId());
			
			// After joining, let's wait for a bit before leaving
			new Thread(() -> {
				try {
					Thread.sleep(15000); // Wait for 15 seconds
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				latch.countDown(); // Signal the main thread to proceed with cleanup
			}).start();
		}
		
		@Override
		public void onParticipantJoined(ParticipantJoinedEvent event) {
			System.out.println(
					"Someone joined the conference: " + event.getParticipant().getDisplayName()
			);
		}
		
		@Override
		public void onParticipantLeft(ParticipantLeftEvent event) {
			System.out.println(
					"Someone left the conference: participant ID " + event.getLeavingParticipantId()
			);
		}
		
		@Override
		public void onParticipantUpdated(ParticipantUpdatedEvent event) {
			// Not used in this simple example
		}
		
		@Override
		public void onRoomDestroyed(RoomDestroyedEvent event) {
			System.out.println("Room was destroyed remotely.");
			latch.countDown(); // Release latch if room is destroyed
		}
		
		@Override
		public void onEvent(JSONObject event) {
		}
	}
}
```
