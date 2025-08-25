package io.github.kinsleykajiva.janus.handle.impl;

import io.github.kinsleykajiva.janus.JanusSession;
import io.github.kinsleykajiva.janus.handle.HandleType;
import io.github.kinsleykajiva.janus.handle.JanusHandle;
import io.github.kinsleykajiva.janus.plugins.audiobridge.listeners.JanusAudioBridgeListener;
import io.github.kinsleykajiva.janus.plugins.audiobridge.models.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONObject;

/**
 * A handle to interact with the Janus AudioBridge plugin. This class is the primary entry point for managing
 * audio conferences. It provides methods to send requests to the plugin (e.g., creating rooms, joining, muting)
 * and manages listeners for asynchronous events, abstracting the underlying JSON-based communication.
 * It is thread-safe.
 */
public class AudioBridgeHandle extends JanusHandle {

    private final List<JanusAudioBridgeListener> audioBridgeListeners = new CopyOnWriteArrayList<>();

    /**
     * Constructs a new handle for the AudioBridge plugin.
     *
     * @param session  The Janus session this handle is associated with.
     * @param handleId The unique ID of this handle.
     */
    public AudioBridgeHandle(JanusSession session, long handleId) {
        super(session, handleId, HandleType.AUDIO_BRIDGE);
    }

    /**
     * Adds a listener to receive events from the AudioBridge plugin. Listeners are notified of events
     * like participants joining, leaving, or being muted.
     *
     * @param listener The listener to add.
     */
    public void addAudioBridgeListener(JanusAudioBridgeListener listener) {
        audioBridgeListeners.add(listener);
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener The listener to remove.
     */
    public void removeAudioBridgeListener(JanusAudioBridgeListener listener) {
        audioBridgeListeners.remove(listener);
    }

    @Override
    public void fireEvent(JSONObject event) {
        if (!event.has("plugindata") || !event.getJSONObject("plugindata").has("data")) {
            return; // Not a valid plugin event
        }
        final var data = event.getJSONObject("plugindata").getJSONObject("data");
        final String eventType = data.optString("audiobridge");

        // Always forward the raw event for debugging or custom handling
        audioBridgeListeners.forEach(listener -> listener.onEvent(data));

        switch (eventType) {
            case "joined":
                // This event is for the local user successfully joining the room.
                final var joinedEvent = JoinedEvent.fromJson(data);
                audioBridgeListeners.forEach(listener -> listener.onJoined(joinedEvent));
                break;
            case "destroyed":
                final var destroyedEvent = RoomDestroyedEvent.fromJson(data);
                audioBridgeListeners.forEach(listener -> listener.onRoomDestroyed(destroyedEvent));
                break;
            case "event":
                // This is a generic event that can signify multiple things.
                if (data.has("leaving")) {
                    // A participant has left the room.
                    final var leftEvent = ParticipantLeftEvent.fromJson(data);
                    audioBridgeListeners.forEach(listener -> listener.onParticipantLeft(leftEvent));
                } else if (data.has("participants")) {
                    // A participant has joined or their state has been updated.
                    // The documentation is slightly ambiguous here. A common pattern is that
                    // a remote participant joining also comes as an 'event'.
                    // We will treat this as a "joined" event for another participant.
                    final var participantJoinedEvent = ParticipantJoinedEvent.fromJson(data);
                    if (participantJoinedEvent != null) {
                        audioBridgeListeners.forEach(listener -> listener.onParticipantJoined(participantJoinedEvent));
                    }

                    // We can also fire an 'updated' event. For simplicity, we assume one event per message.
                    // A more advanced implementation might check if the participant ID is new or existing.
                    final var participantUpdatedEvent = ParticipantUpdatedEvent.fromJson(data);
                    if (participantUpdatedEvent != null) {
                        audioBridgeListeners.forEach(listener -> listener.onParticipantUpdated(participantUpdatedEvent));
                    }
                }
                break;
            // Other event types like 'roomchanged', 'announcement-started' can be added here in the future.
        }
    }

    /**
     * Sends a request to create a new AudioBridge room with the specified configuration.
     *
     * @param request A {@link CreateRoomRequest} object containing the desired room settings.
     * @return A {@link CompletableFuture} that will be completed with an {@link AudioBridgeRoom} object
     *         representing the newly created room.
     */
    public CompletableFuture<AudioBridgeRoom> createRoom(CreateRoomRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("created".equals(pluginData.optString("audiobridge"))) {
                return AudioBridgeRoom.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to create room: " + response.toString());
            }
        });
    }

    /**
     * Destroys an existing AudioBridge room.
     *
     * @param roomId The unique ID of the room to destroy.
     * @param secret The secret required to manage the room, if any.
     * @return A {@link CompletableFuture} that completes when the room has been successfully destroyed.
     */
    public CompletableFuture<Void> destroyRoom(long roomId, String secret) {
        final var body = new JSONObject()
            .put("request", "destroy")
            .put("room", roomId)
            .put("secret", secret);
        return sendMessage(body).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"destroyed".equals(pluginData.optString("audiobridge"))) {
                 throw new RuntimeException("Failed to destroy room: " + response.toString());
            }
        });
    }

    /**
     * Retrieves a list of all available (non-private) AudioBridge rooms.
     *
     * @return A {@link CompletableFuture} that completes with a list of {@link AudioBridgeRoom} objects.
     */
    public CompletableFuture<List<AudioBridgeRoom>> listRooms() {
        final var body = new JSONObject().put("request", "list");
        return sendMessage(body).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            final var list = pluginData.getJSONArray("list");
            return IntStream.range(0, list.length())
                .mapToObj(list::getJSONObject)
                .map(AudioBridgeRoom::fromJson)
                .collect(Collectors.toList());
        });
    }

    /**
     * Sends a request for the local user to join a room. This is an asynchronous operation.
     * A successful join will be indicated by an {@code onJoined} event on registered listeners.
     *
     * @param request A {@link JoinRoomRequest} with the details for joining the room.
     * @return A {@link CompletableFuture} that completes when the join request has been acknowledged by the server.
     */
    public CompletableFuture<Void> joinRoom(JoinRoomRequest request) {
        // Asynchronous, completion is signaled by a `joined` event.
        return sendMessage(request.toJson()).thenAccept(response -> {
            // The response to an async request can be a simple 'ack'.
            // We just check that it's not an error. The substantive event (e.g., 'joined')
            // will be delivered to the listener separately.
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on join request: " + response.toString());
            }
        });
    }

    /**
     * Configures the local user's participation in the room (e.g., muting or changing display name).
     *
     * @param request A {@link ConfigureRequest} object with the desired configuration changes.
     * @return A {@link CompletableFuture} that completes when the configure request is acknowledged.
     */
    public CompletableFuture<Void> configure(ConfigureRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            // The response to an async request can be a simple 'ack'.
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on configure request: " + response.toString());
            }
        });
    }

    /**
     * Sends a request for the local user to leave the current room. A successful departure will
     * be indicated by a `left` event from the server.
     *
     * @return A {@link CompletableFuture} that completes when the leave request is acknowledged.
     */
    public CompletableFuture<Void> leave() {
        final var body = new JSONObject().put("request", "leave");
        return sendMessage(body).thenAccept(response -> {
            // The response to an async request can be a simple 'ack'.
             if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on leave request: " + response.toString());
            }
        });
    }

    /**
     * Retrieves the list of participants currently in a specific room.
     *
     * @param roomId The unique ID of the room.
     * @return A {@link CompletableFuture} that completes with a list of {@link AudioBridgeParticipant} objects.
     */
    public CompletableFuture<List<AudioBridgeParticipant>> listParticipants(long roomId) {
        final var body = new JSONObject()
            .put("request", "listparticipants")
            .put("room", roomId);

        return sendMessage(body).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            final var participantsJson = pluginData.getJSONArray("participants");
            return IntStream.range(0, participantsJson.length())
                .mapToObj(participantsJson::getJSONObject)
                .map(AudioBridgeParticipant::fromJson)
                .collect(Collectors.toList());
        });
    }
}