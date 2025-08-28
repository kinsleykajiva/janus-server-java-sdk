package io.github.kinsleykajiva.janus.client.handle.impl;

import io.github.kinsleykajiva.janus.client.JanusSession;
import io.github.kinsleykajiva.janus.client.handle.HandleType;
import io.github.kinsleykajiva.janus.client.handle.JanusHandle;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.events.*;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.listeners.JanusAudioBridgeListener;
import io.github.kinsleykajiva.janus.client.plugins.audiobridge.models.*;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            case "announcement-started":
                final var announcementStartedEvent = AnnouncementStartedEvent.fromJson(data);
                audioBridgeListeners.forEach(listener -> listener.onAnnouncementStarted(announcementStartedEvent));
                break;
            case "announcement-stopped":
                final var announcementStoppedEvent = AnnouncementStoppedEvent.fromJson(data);
                audioBridgeListeners.forEach(listener -> listener.onAnnouncementStopped(announcementStoppedEvent));
                break;
            case "roomchanged":
                final var roomChangedEvent = RoomChangedEvent.fromJson(data);
                audioBridgeListeners.forEach(listener -> listener.onRoomChanged(roomChangedEvent));
                break;
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
     * @return A {@link CompletableFuture} that completes when the room has been successfully destroyed.
     */
    public CompletableFuture<Void> destroyRoom(DestroyRoomRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
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

    public CompletableFuture<Void> editRoom(EditRoomRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"edited".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to edit room: " + response.toString());
            }
        });
    }

    public CompletableFuture<ExistsResponse> exists(ExistsRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to check if room exists: " + response.toString());
            }
            return ExistsResponse.fromJson(pluginData);
        });
    }

    public CompletableFuture<AllowedResponse> allowed(AllowedRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to manage allowed list: " + response.toString());
            }
            return AllowedResponse.fromJson(pluginData);
        });
    }

    public CompletableFuture<Void> kick(KickRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to kick participant: " + response.toString());
            }
        });
    }

    public CompletableFuture<Void> kickAll(KickAllRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to kick all participants: " + response.toString());
            }
        });
    }

    public CompletableFuture<Void> suspend(SuspendParticipantRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to suspend participant: " + response.toString());
            }
        });
    }

    public CompletableFuture<Void> resume(ResumeParticipantRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to resume participant: " + response.toString());
            }
        });
    }

    public CompletableFuture<Void> mute(MuteParticipantRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to mute participant: " + response.toString());
            }
        });
    }

    public CompletableFuture<Void> unmute(UnmuteParticipantRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to unmute participant: " + response.toString());
            }
        });
    }

    public CompletableFuture<Void> muteRoom(MuteRoomRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to mute room: " + response.toString());
            }
        });
    }

    public CompletableFuture<Void> unmuteRoom(UnmuteRoomRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to unmute room: " + response.toString());
            }
        });
    }

    public CompletableFuture<ListAnnouncementsResponse> listAnnouncements(ListAnnouncementsRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"announcements".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to list announcements: " + response.toString());
            }
            return ListAnnouncementsResponse.fromJson(pluginData);
        });
    }

    public CompletableFuture<Void> resetDecoder() {
        return sendMessage(new ResetDecoderRequest().toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to reset decoder: " + response.toString());
            }
        });
    }

    public CompletableFuture<RtpForwardResponse> rtpForward(RtpForwardRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to forward rtp: " + response.toString());
            }
            return RtpForwardResponse.fromJson(pluginData);
        });
    }

    public CompletableFuture<Void> stopRtpForward(StopRtpForwardRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to stop rtp forward: " + response.toString());
            }
        });
    }

    public CompletableFuture<ListForwardersResponse> listForwarders(ListForwardersRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"forwarders".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to list forwarders: " + response.toString());
            }
            return ListForwardersResponse.fromJson(pluginData);
        });
    }

    public CompletableFuture<PlayFileResponse> playFile(PlayFileRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to play file: " + response.toString());
            }
            return PlayFileResponse.fromJson(pluginData);
        });
    }

    public CompletableFuture<IsPlayingResponse> isPlaying(IsPlayingRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to check if file is playing: " + response.toString());
            }
            return IsPlayingResponse.fromJson(pluginData);
        });
    }

    public CompletableFuture<StopFileResponse> stopFile(StopFileRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to stop file: " + response.toString());
            }
            return StopFileResponse.fromJson(pluginData);
        });
    }

    public CompletableFuture<StopAllFilesResponse> stopAllFiles(StopAllFilesRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to stop all files: " + response.toString());
            }
            return StopAllFilesResponse.fromJson(pluginData);
        });
    }

    public CompletableFuture<Void> changeRoom(ChangeRoomRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on change room request: " + response.toString());
            }
        });
    }

    public CompletableFuture<Void> enableRecording(EnableRecordingRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to enable recording: " + response.toString());
            }
        });
    }

    public CompletableFuture<Void> enableMjrs(EnableMjrsRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("audiobridge"))) {
                throw new RuntimeException("Failed to enable mjrs: " + response.toString());
            }
        });
    }
}