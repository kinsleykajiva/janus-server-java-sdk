package io.github.kinsleykajiva.models.events;

/** Enum representing various types of Janus session events. */
public enum EventType {
  SESSION(1, "Session related event"),
  HANDLE(2, "Handle related event"),
  EXTERNAL(4, "External event (injected via Admin API)"),
  JSEP(8, "JSEP event (SDP offer/answer)"),
  WEBRTC_STATE(16, "WebRTC state event (ICE/DTLS states, candidates, etc.)"),
  MEDIA(32, "Media event (media state, reports, etc.)"),
  PLUGIN(64, "Plugin-originated event (e.g., event coming from VideoRoom)"),
  TRANSPORT(128, "Transport-originated event (e.g., WebSocket connection state)"),
  CORE(256, "Core event (server startup/shutdown)");

  private final int typeValue;
  private final String eventDescription;

  EventType(int typeValue, String eventDescription) {
    this.typeValue = typeValue;
    this.eventDescription = eventDescription;
  }

  /**
   * Returns the type value of the event.
   *
   * @return the type value of the event
   */
  public int getTypeValue() {
    return typeValue;
  }

  /**
   * Returns the description of the event.
   *
   * @return the description of the event
   */
  public String getEventDescription() {
    return eventDescription;
  }

  /**
   * Returns the event type corresponding to the type value.
   *
   * @param typeValue the type value of the event
   * @return the event type corresponding to the type value
   */
  public static EventType fromTypeValue(int typeValue) {
    for (EventType eventType : EventType.values()) {
      if (eventType.getTypeValue() == typeValue) {
        return eventType;
      }
    }
    return null;
  }
}
