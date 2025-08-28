package io.github.kinsleykajiva.janus.client.plugins.videoroom.listeners;

import io.github.kinsleykajiva.janus.client.plugins.videoroom.events.*;
import org.json.JSONObject;

/**
 * A listener interface for receiving asynchronous events from the Janus VideoRoom plugin.
 * Implement this interface to handle real-time notifications from the video conference.
 * All methods are default to allow for selective implementation.
 */
public interface JanusVideoRoomListener {

    /**
     * Called when the local user has successfully joined a room as a publisher.
     *
     * @param event The {@link JoinedEvent} containing details about the room and other participants.
     */
    default void onJoined(JoinedEvent event) {}

    /**
     * Called when a new publisher becomes active in the room.
     *
     * @param event The {@link PublisherAddedEvent} containing the details of the new publisher(s).
     */
    default void onPublisherAdded(PublisherAddedEvent event) {}

    /**
     * Called when a publisher leaves or otherwise stops publishing.
     *
     * @param event The {@link UnpublishedEvent} containing the ID of the publisher who unpublished.
     */
    default void onUnpublished(UnpublishedEvent event) {}

    /**
     * Called when a participant leaves the room.
     *
     * @param event The {@link ParticipantLeftEvent} containing the ID of the participant who left.
     */
    default void onParticipantLeft(ParticipantLeftEvent event) {}

    /**
     * Called when a room is destroyed.
     *
     * @param event The {@link RoomDestroyedEvent} containing the ID of the destroyed room.
     */
    default void onRoomDestroyed(RoomDestroyedEvent event) {}

    /**
     * Called when a subscriber handle has been successfully attached and configured.
     * This event is accompanied by a JSEP offer from the plugin.
     *
     * @param event The {@link AttachedEvent} containing details of the subscribed-to streams.
     */
    default void onSubscriberAttached(AttachedEvent event) {}

    /**
     * A generic callback for any event from the VideoRoom plugin. This is useful for
     * debugging or handling events not explicitly covered by other methods.
     *
     * @param event The raw {@link JSONObject} of the event's `plugindata.data` field.
     */
    default void onEvent(JSONObject event) {}

    /**
     * Called when a publisher has started talking.
     *
     * @param event The {@link TalkingEvent} containing the publisher's ID.
     */
    default void onTalking(TalkingEvent event) {}

    /**
     * Called when a publisher has stopped talking.
     *
     * @param event The {@link StoppedTalkingEvent} containing the publisher's ID.
     */
    default void onStoppedTalking(StoppedTalkingEvent event) {}

    /**
     * Called when a subscription is updated, e.g., after an `update` request.
     *
     * @param event The {@link UpdatedEvent} containing the new stream layout.
     */
    default void onSubscriptionUpdated(UpdatedEvent event) {}

    /**
     * Called when a stream has been successfully switched to a new source.
     *
     * @param event The {@link SwitchedEvent} containing details of the switch.
     */
    default void onSwitched(SwitchedEvent event) {}
}
