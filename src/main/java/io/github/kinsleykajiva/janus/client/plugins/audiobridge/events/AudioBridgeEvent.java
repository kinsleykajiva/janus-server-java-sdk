package io.github.kinsleykajiva.janus.client.plugins.audiobridge.events;

/**
 * A base record for all AudioBridge events, containing the room ID.
 *
 * @param roomId The unique numeric ID of the room where the event occurred.
 */
public record AudioBridgeEvent(long roomId) {
}
