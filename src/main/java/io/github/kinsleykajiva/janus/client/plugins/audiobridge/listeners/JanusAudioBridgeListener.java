package io.github.kinsleykajiva.janus.client.plugins.audiobridge.listeners;

import io.github.kinsleykajiva.janus.client.plugins.audiobridge.events.*;
import org.json.JSONObject;

/**
 * A listener interface for receiving events from the Janus AudioBridge plugin.
 * Implement this interface to handle asynchronous notifications from the audio conference.
 */
public interface JanusAudioBridgeListener {

    /**
     * Called when the local user has successfully joined a room.
     *
     * @param event The {@link JoinedEvent} containing details about the room and other participants.
     */
    void onJoined(JoinedEvent event);

    /**
     * Called when a new participant joins the room.
     *
     * @param event The {@link ParticipantJoinedEvent} containing the details of the new participant.
     */
    void onParticipantJoined(ParticipantJoinedEvent event);

    /**
     * Called when a participant leaves the room.
     *
     * @param event The {@link ParticipantLeftEvent} containing the ID of the participant who left.
     */
    void onParticipantLeft(ParticipantLeftEvent event);

    /**
     * Called when a participant's state is updated (e.g., muted, unmuted, suspended).
     *
     * @param event The {@link ParticipantUpdatedEvent} containing the updated details of the participant.
     */
    void onParticipantUpdated(ParticipantUpdatedEvent event);

    /**
     * Called when a room is destroyed.
     *
     * @param event The {@link RoomDestroyedEvent} containing the ID of the destroyed room.
     */
    void onRoomDestroyed(RoomDestroyedEvent event);

    /**
     * Called when an announcement has started in a room.
     *
     * @param event The {@link AnnouncementStartedEvent} containing the details of the announcement.
     */
    default void onAnnouncementStarted(AnnouncementStartedEvent event) {
        // Default implementation does nothing.
    }

    /**
     * Called when an announcement has stopped in a room.
     *
     * @param event The {@link AnnouncementStoppedEvent} containing the details of the announcement.
     */
    default void onAnnouncementStopped(AnnouncementStoppedEvent event) {
        // Default implementation does nothing.
    }

    /**
     * Called when the local user has successfully changed rooms.
     *
     * @param event The {@link RoomChangedEvent} containing the details of the new room.
     */
    default void onRoomChanged(RoomChangedEvent event) {
        // Default implementation does nothing.
    }

    /**
     * A generic callback for any event from the AudioBridge plugin.
     * This can be used for debugging or handling events not explicitly covered by other methods.
     *
     * @param event The raw {@link JSONObject} of the event.
     */
    default void onEvent(JSONObject event) {
        // Default implementation does nothing.
    }
}
