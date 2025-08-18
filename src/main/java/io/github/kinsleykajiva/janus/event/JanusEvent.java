package io.github.kinsleykajiva.janus.event;

import org.json.JSONObject;

/**
 * Represents an asynchronous event received from the Janus Gateway.
 * @param eventData The full JSON data of the event.
 * @param jsep Optional JSEP (SDP) data associated with the event.
 */
public record JanusEvent(JSONObject eventData, JSONObject jsep) {}