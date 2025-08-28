package io.github.kinsleykajiva.janus.client.handle.impl;

import io.github.kinsleykajiva.janus.client.JanusSession;
import io.github.kinsleykajiva.janus.client.handle.HandleType;
import io.github.kinsleykajiva.janus.client.handle.JanusHandle;
import io.github.kinsleykajiva.janus.client.plugins.videoroom.events.*;
import io.github.kinsleykajiva.janus.client.plugins.videoroom.listeners.JanusVideoRoomListener;
import io.github.kinsleykajiva.janus.client.plugins.videoroom.models.*;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A handle to interact with the Janus VideoRoom plugin. This class provides methods to send requests
 * to the plugin (e.g., creating rooms, joining, publishing) and manages listeners for asynchronous events.
 * It abstracts the underlying JSON-based communication and is thread-safe.
 */
public class VideoRoomHandle extends JanusHandle {

    private final List<JanusVideoRoomListener> videoRoomListeners = new CopyOnWriteArrayList<>();

    /**
     * Constructs a new handle for the VideoRoom plugin.
     *
     * @param session  The Janus session this handle is associated with.
     * @param handleId The unique ID of this handle.
     */
    public VideoRoomHandle(JanusSession session, long handleId) {
        super(session, handleId, HandleType.VIDEO_ROOM);
    }

    /**
     * Adds a listener to receive events from the VideoRoom plugin.
     * @param listener The listener to add.
     */
    public void addVideoRoomListener(JanusVideoRoomListener listener) {
        videoRoomListeners.add(listener);
    }

    /**
     * Removes a previously registered listener.
     * @param listener The listener to remove.
     */
    public void removeVideoRoomListener(JanusVideoRoomListener listener) {
        videoRoomListeners.remove(listener);
    }

    @Override
    public void fireEvent(JSONObject event) {
        if (!event.has("plugindata") || !event.getJSONObject("plugindata").has("data")) {
            return; // Not a valid plugin event
        }
        final var data = event.getJSONObject("plugindata").getJSONObject("data");
        final String eventType = data.optString("videoroom");

        // Always forward the raw event for debugging or custom handling
        videoRoomListeners.forEach(listener -> listener.onEvent(data));

        switch (eventType) {
            case "joined":
                videoRoomListeners.forEach(listener -> listener.onJoined(JoinedEvent.fromJson(data)));
                break;
            case "destroyed":
                videoRoomListeners.forEach(listener -> listener.onRoomDestroyed(RoomDestroyedEvent.fromJson(data)));
                break;
            case "attached":
                videoRoomListeners.forEach(listener -> listener.onSubscriberAttached(AttachedEvent.fromJson(data)));
                break;
            case "updated":
                videoRoomListeners.forEach(listener -> listener.onSubscriptionUpdated(UpdatedEvent.fromJson(data)));
                break;
            case "talking":
                videoRoomListeners.forEach(listener -> listener.onTalking(TalkingEvent.fromJson(data)));
                break;
            case "stopped-talking":
                videoRoomListeners.forEach(listener -> listener.onStoppedTalking(StoppedTalkingEvent.fromJson(data)));
                break;
            case "event":
                // This is a generic container for other events
                if (data.has("publishers")) {
                    videoRoomListeners.forEach(listener -> listener.onPublisherAdded(PublisherAddedEvent.fromJson(data)));
                } else if (data.has("unpublished")) {
                    videoRoomListeners.forEach(listener -> listener.onUnpublished(UnpublishedEvent.fromJson(data)));
                } else if (data.has("leaving")) {
                    videoRoomListeners.forEach(listener -> listener.onParticipantLeft(ParticipantLeftEvent.fromJson(data)));
                } else if (data.has("switched") && "ok".equals(data.optString("switched"))) {
                    videoRoomListeners.forEach(listener -> listener.onSwitched(SwitchedEvent.fromJson(data)));
                }
                break;
        }
    }

    /**
     * Creates a new video room.
     * @param request A {@link CreateRoomRequest} with the desired room settings.
     * @return A {@link CompletableFuture} that completes with a {@link CreateRoomResponse}.
     */
    public CompletableFuture<CreateRoomResponse> createRoom(CreateRoomRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("created".equals(pluginData.optString("videoroom"))) {
                return CreateRoomResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to create room: " + response);
            }
        });
    }

    /**
     * Destroys an existing video room.
     * @param request A {@link DestroyRoomRequest} specifying the room to destroy.
     * @return A {@link CompletableFuture} that completes with a {@link DestroyRoomResponse}.
     */
    public CompletableFuture<DestroyRoomResponse> destroyRoom(DestroyRoomRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("destroyed".equals(pluginData.optString("videoroom"))) {
                return DestroyRoomResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to destroy room: " + response);
            }
        });
    }

     /**
     * Edits an existing video room.
     * @param request A {@link EditRoomRequest} with the new settings.
     * @return A {@link CompletableFuture} that completes with an {@link EditRoomResponse}.
     */
    public CompletableFuture<EditRoomResponse> editRoom(EditRoomRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("edited".equals(pluginData.optString("videoroom"))) {
                return EditRoomResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to edit room: " + response);
            }
        });
    }

    /**
     * Checks if a room exists.
     * @param request An {@link ExistsRequest} specifying the room to check.
     * @return A {@link CompletableFuture} that completes with an {@link ExistsResponse}.
     */
    public CompletableFuture<ExistsResponse> exists(ExistsRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("success".equals(pluginData.optString("videoroom"))) {
                return ExistsResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to check if room exists: " + response);
            }
        });
    }

    /**
     * Lists all available (non-private) video rooms.
     * @return A {@link CompletableFuture} that completes with a {@link ListRoomsResponse}.
     */
    public CompletableFuture<ListRoomsResponse> listRooms() {
        return sendMessage(new ListRoomsRequest().toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("success".equals(pluginData.optString("videoroom"))) {
                return ListRoomsResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to list rooms: " + response);
            }
        });
    }

    /**
     * Lists all participants in a specific room.
     * @param request A {@link ListParticipantsRequest} specifying the room.
     * @return A {@link CompletableFuture} that completes with a {@link ListParticipantsResponse}.
     */
    public CompletableFuture<ListParticipantsResponse> listParticipants(ListParticipantsRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("participants".equals(pluginData.optString("videoroom"))) {
                return ListParticipantsResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to list participants: " + response);
            }
        });
    }

    /**
     * Manages the token-based ACL for a room.
     * @param request An {@link AllowedRequest} specifying the action and tokens.
     * @return A {@link CompletableFuture} that completes with an {@link AllowedResponse}.
     */
    public CompletableFuture<AllowedResponse> allowed(AllowedRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("success".equals(pluginData.optString("videoroom"))) {
                return AllowedResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to manage allowed list: " + response);
            }
        });
    }

    /**
     * Kicks a participant from a room. This is a synchronous operation.
     * @param request A {@link KickRequest} specifying the room and participant.
     * @return A {@link CompletableFuture} that completes when the request is successful.
     */
    public CompletableFuture<Void> kick(KickRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("videoroom"))) {
                throw new RuntimeException("Failed to kick participant: " + response);
            }
        });
    }

    /**
     * Mutes or unmutes a participant's media stream. This is a synchronous operation.
     * @param request A {@link ModerateRequest} specifying the participant and action.
     * @return A {@link CompletableFuture} that completes when the request is successful.
     */
    public CompletableFuture<Void> moderate(ModerateRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("videoroom"))) {
                throw new RuntimeException("Failed to moderate participant: " + response);
            }
        });
    }

    /**
     * Globally enables or disables recording for all participants in a room.
     * @param request An {@link EnableRecordingRequest} specifying the action.
     * @return A {@link CompletableFuture} that completes when the request is successful.
     */
    public CompletableFuture<Void> enableRecording(EnableRecordingRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("videoroom"))) {
                throw new RuntimeException("Failed to enable/disable recording: " + response);
            }
        });
    }

    /**
     * Starts forwarding a publisher's media streams to a remote RTP listener.
     * @param request An {@link RtpForwardRequest} with the forwarding details.
     * @return A {@link CompletableFuture} that completes with an {@link RtpForwardResponse}.
     */
    public CompletableFuture<RtpForwardResponse> rtpForward(RtpForwardRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("rtp_forward".equals(pluginData.optString("videoroom"))) {
                return RtpForwardResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to start RTP forward: " + response);
            }
        });
    }

    /**
     * Stops an active RTP forwarder.
     * @param request A {@link StopRtpForwardRequest} specifying the forwarder to stop.
     * @return A {@link CompletableFuture} that completes when the request is successful.
     */
    public CompletableFuture<Void> stopRtpForward(StopRtpForwardRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"stop_rtp_forward".equals(pluginData.optString("videoroom"))) {
                 // Note: The success response for this is actually an event, not a direct reply.
                 // The check here is a safeguard; a `stop_rtp_forward` field in a success response is unusual.
                 // We rely on the generic success of sendMessage.
            }
        });
    }

    /**
     * Lists all active RTP forwarders in a room.
     * @param request A {@link ListForwardersRequest} specifying the room.
     * @return A {@link CompletableFuture} that completes with a {@link ListForwardersResponse}.
     */
    public CompletableFuture<ListForwardersResponse> listForwarders(ListForwardersRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("forwarders".equals(pluginData.optString("videoroom"))) {
                return ListForwardersResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to list RTP forwarders: " + response);
            }
        });
    }

    /**
     * Sends a request for a publisher to join a room. This is an asynchronous operation.
     * A successful join will be indicated by an {@code onJoined} event.
     *
     * @param request A {@link JoinRoomRequest} with the details for joining the room.
     * @return A {@link CompletableFuture} that completes when the join request has been acknowledged.
     */
    public CompletableFuture<Void> join(JoinRoomRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on join request: " + response);
            }
        });
    }

    /**
     * Joins a room and configures a stream to be published in a single request.
     * This is an asynchronous operation. The response will contain the JSEP answer.
     *
     * @param request A {@link JoinAndConfigureRequest} with the details for joining and publishing.
     * @param jsep A {@link JSONObject} containing the JSEP offer (e.g., `{"type": "offer", "sdp": "..."}`).
     * @return A {@link CompletableFuture} that completes with the full response from Janus, including the JSEP answer.
     */
    public CompletableFuture<JSONObject> joinAndConfigure(JoinAndConfigureRequest request, JSONObject jsep) {
        return sendMessage(request.toJson(), jsep);
    }

    /**
     * Sends a request to publish a media stream. This is an asynchronous operation and must be
     * accompanied by a JSEP offer in the top-level message. A `configured` event will be sent
     * in response, followed by a `publisher-added` event to all participants.
     *
     * @param request A {@link PublishRequest} with the details of the stream to publish.
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> publish(PublishRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
             if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on publish request: " + response);
            }
        });
    }

    /**
     * Sends a request to publish a media stream, including a JSEP offer.
     * This is an asynchronous operation. The response will contain the JSEP answer.
     *
     * @param request A {@link PublishRequest} with the details of the stream to publish.
     * @param jsep A {@link JSONObject} containing the JSEP offer (e.g., `{"type": "offer", "sdp": "..."}`).
     * @return A {@link CompletableFuture} that completes with the full response from Janus, including the JSEP answer.
     */
    public CompletableFuture<JSONObject> publish(PublishRequest request, JSONObject jsep) {
        return sendMessage(request.toJson(), jsep);
    }

    /**
     * Configures an active publisher session. This can be used to change bitrate, display name,
     * or tweak individual media streams. This is an asynchronous operation.
     *
     * @param request A {@link ConfigurePublisherRequest} with the desired configuration changes.
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> configure(ConfigurePublisherRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
               throw new RuntimeException("Janus returned an error on configure publisher request: " + response);
           }
        });
    }

    /**
     * Configures an active subscriber session. This can be used to change stream properties
     * (e.g., switch simulcast layers) or trigger an ICE restart. This is an asynchronous operation.
     *
     * @param request A {@link ConfigureSubscriberRequest} with the desired configuration changes.
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> configure(ConfigureSubscriberRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
               throw new RuntimeException("Janus returned an error on configure subscriber request: " + response);
           }
        });
    }

    /**
     * Sends a request to subscribe to one or more streams. This is an asynchronous operation.
     * A successful subscription will result in an `attached` event accompanied by a JSEP offer.
     *
     * @param request A {@link SubscribeRequest} with the details of the streams to subscribe to.
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> subscribe(SubscribeRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
             if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on subscribe request: " + response);
            }
        });
    }

    /**
     * Sends a request to start receiving media for a subscription. This must be accompanied by a
     * JSEP answer to the offer received in the `attached` event.
     *
     * @param request A {@link StartSubscriptionRequest}.
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> start(StartSubscriptionRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
             if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on start request: " + response);
            }
        });
    }

    /**
     * Sends a request to start receiving media for a subscription, including a JSEP answer.
     * This is an asynchronous operation.
     *
     * @param request A {@link StartSubscriptionRequest}.
     * @param jsep A {@link JSONObject} containing the JSEP answer (e.g., `{"type": "answer", "sdp": "..."}`).
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> start(StartSubscriptionRequest request, JSONObject jsep) {
        return sendMessage(request.toJson(), jsep).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on start request: " + response);
            }
        });
    }

    /**
     * Pauses the media delivery for a subscription. This is an asynchronous operation.
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> pause() {
        return sendMessage(new PauseSubscriptionRequest().toJson()).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on pause request: " + response);
            }
        });
    }

    /**
     * Resumes a paused subscription. This is an asynchronous operation.
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> start() {
        return sendMessage(new StartSubscriptionRequest().toJson()).thenAccept(response -> {
             if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on start/resume request: " + response);
            }
        });
    }

    /**
     * Updates a subscription by subscribing to new streams and/or unsubscribing from others.
     * This is an asynchronous operation that may trigger a renegotiation.
     * @param request An {@link UpdateSubscriptionRequest} with the streams to add/remove.
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> updateSubscription(UpdateSubscriptionRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on update request: " + response);
            }
        });
    }

    /**
     * Switches a subscriber's stream to a different publisher without a full renegotiation.
     * This is an asynchronous operation.
     * @param request A {@link SwitchRequest} with the details of the switch operation.
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> switchRequest(SwitchRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on switch request: " + response);
            }
        });
    }

    /**
     * Sends a request to unpublish a media stream. This is an asynchronous operation.
     * A successful unpublish will result in an `unpublished` event.
     *
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> unpublish() {
        return sendMessage(new UnpublishRequest().toJson()).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on unpublish request: " + response);
            }
        });
    }

    /**
     * Sends a request to leave the room (for publishers) or close a subscription (for subscribers).
     * This is an asynchronous operation. A successful leave will result in a `leaving` event.
     *
     * @return A {@link CompletableFuture} that completes when the request has been acknowledged.
     */
    public CompletableFuture<Void> leave() {
        return sendMessage(new LeaveRequest().toJson()).thenAccept(response -> {
            if ("error".equals(response.optString("janus"))) {
                throw new RuntimeException("Janus returned an error on leave request: " + response);
            }
        });
    }

    // --- Remote Publisher (Cascading) Methods ---

    /**
     * Adds a new remote publisher to a room on a remote Janus instance.
     * @param request An {@link AddRemotePublisherRequest} with the remote publisher's details.
     * @return A {@link CompletableFuture} that completes with an {@link AddRemotePublisherResponse}.
     */
    public CompletableFuture<AddRemotePublisherResponse> addRemotePublisher(AddRemotePublisherRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("success".equals(pluginData.optString("videoroom"))) {
                return AddRemotePublisherResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to add remote publisher: " + response);
            }
        });
    }

    /**
     * Updates an existing remote publisher.
     * @param request An {@link UpdateRemotePublisherRequest} with the updated details.
     * @return A {@link CompletableFuture} that completes when the request is successful.
     */
    public CompletableFuture<Void> updateRemotePublisher(UpdateRemotePublisherRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("videoroom"))) {
                throw new RuntimeException("Failed to update remote publisher: " + response);
            }
        });
    }

    /**
     * Removes a remote publisher from a room.
     * @param request A {@link RemoveRemotePublisherRequest} specifying the publisher to remove.
     * @return A {@link CompletableFuture} that completes when the request is successful.
     */
    public CompletableFuture<Void> removeRemotePublisher(RemoveRemotePublisherRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("videoroom"))) {
                throw new RuntimeException("Failed to remove remote publisher: " + response);
            }
        });
    }

    /**
     * Starts relaying a local publisher's stream to a remote Janus instance.
     * @param request A {@link PublishRemotelyRequest} with the connection details.
     * @return A {@link CompletableFuture} that completes with a {@link PublishRemotelyResponse}.
     */
    public CompletableFuture<PublishRemotelyResponse> publishRemotely(PublishRemotelyRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("success".equals(pluginData.optString("videoroom"))) {
                return PublishRemotelyResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to publish remotely: " + response);
            }
        });
    }

    /**
     * Stops a specific remotization of a local publisher.
     * @param request An {@link UnpublishRemotelyRequest} specifying the remotization to stop.
     * @return A {@link CompletableFuture} that completes when the request is successful.
     */
    public CompletableFuture<Void> unpublishRemotely(UnpublishRemotelyRequest request) {
        return sendMessage(request.toJson()).thenAccept(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if (!"success".equals(pluginData.optString("videoroom"))) {
                throw new RuntimeException("Failed to unpublish remotely: " + response);
            }
        });
    }

    /**
     * Lists all active remotizations for a local publisher.
     * @param request A {@link ListRemotesRequest} specifying the publisher.
     * @return A {@link CompletableFuture} that completes with a {@link ListRemotesResponse}.
     */
    public CompletableFuture<ListRemotesResponse> listRemotes(ListRemotesRequest request) {
        return sendMessage(request.toJson()).thenApply(response -> {
            final var pluginData = response.getJSONObject("plugindata").getJSONObject("data");
            if ("success".equals(pluginData.optString("videoroom"))) {
                return ListRemotesResponse.fromJson(pluginData);
            } else {
                throw new RuntimeException("Failed to list remotes: " + response);
            }
        });
    }
}
