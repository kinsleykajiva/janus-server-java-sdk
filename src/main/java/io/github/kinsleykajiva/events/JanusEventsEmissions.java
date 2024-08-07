package io.github.kinsleykajiva.events;

import io.github.kinsleykajiva.models.events.EventType;
import io.github.kinsleykajiva.utils.SdkUtils;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.json.JSONArray;
import org.json.JSONObject;

public interface JanusEventsEmissions {

  // !	ExecutorService executorService =
  // Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());// this is for java
  // jdk 20 and less versons
  ExecutorService executorService =  Executors.newVirtualThreadPerTaskExecutor(); // ! this is for java jdk 21 and  above

  /**
   * Callback method called when a participant joins a room.
   *
   * @param participantId The ID of the participant who joined the room.
   * @param participantDisplay The display name of the participant who joined the room.
   * @param roomId The ID of the room which the participant joined.
   */
  void onParticipantJoined(long participantId, String participantDisplay, String roomId);

  /**
   * Callback method called when a participant leaves a room.
   *
   * @param participantId The ID of the participant who left the room.
   * @param participantDisplay The display name of the participant who left the room.
   * @param roomId The ID of the room from which the participant left.
   */
  void onParticipantLeft(long participantId, String participantDisplay, String roomId);

  /**
   * Event fired when the first participant joins the room.
   *
   * @param roomId The room id.
   * @param firstParticipantId The first participant id.
   * @param firstParticipantDisplay The first participant display name.
   */
  void onRoomSessionStarted(String roomId, long firstParticipantId, String firstParticipantDisplay);

  /**
   * Callback method called when a room session ends.
   *
   * @param roomId The ID of the room that ended the session.
   */
  void onRoomSessionEnded(String roomId);
  
  /**
   * Event fired when a new event is received from Janus.
   *
   * @param event The event JSON dump. If the event is null or not a valid JSON, an error message will be printed.
   *              If an exception occurs during processing, it will be printed to the console.
   */
  default void consumeEventAsync(String event) {
    if (Objects.isNull(event)) {
      System.err.println("Event is not a valid JSON");
      return;
    }
    if (!SdkUtils.isJson(event)) {
      System.err.println("Event is not a valid JSON");
      return;
    }
    
    try {
    JSONArray jsonArray = SdkUtils.isJsonArray(event) ? new JSONArray(event) : new JSONArray().put(new JSONObject(event));

    IntStream.range(0, jsonArray.length())
        .parallel()
        .mapToObj(jsonArray::getJSONObject)
        .forEach(jsonEvent -> executorService.submit(() -> processEvent(jsonEvent)));
    
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void processEvent(JSONObject jsonEvent) {
    
    var janusEventsFactory = new JanusEventsFactory(jsonEvent, this);
    EventType eventType    = EventType.fromTypeValue(jsonEvent.getInt("type"));

    switch (eventType) {
      case CORE          -> janusEventsFactory.processEvent256();
      case TRANSPORT     -> janusEventsFactory.processEvent128();
      case WEBRTC_STATE  -> janusEventsFactory.processEvent16();
      case HANDLE        -> janusEventsFactory.processEvent2();
      case MEDIA         -> janusEventsFactory.processEvent32();
      case JSEP          -> janusEventsFactory.processEvent8();
      case SESSION       -> janusEventsFactory.processEvent1();
      case PLUGIN        -> janusEventsFactory.processVideoRoomEvent(jsonEvent);
	  case null, default -> {}
    }
  }
}
