package io.github.kinsleykajiva.models.events;

/** Represents the data associated with a video room plugin event. */
public class VideoRoomPluginEventData extends Event {
  private String event;
  private String room;
  private String display;
  private String opaque_id;
  private long id = 0;
  private long bitrate = 0;
  private long private_id = 0;
  private VideoRoomPluginEventDataStream[] stream;

  public String getOpaque_id() {
    return opaque_id;
  }

  public void setOpaque_id(String opaque_id) {
    this.opaque_id = opaque_id;
  }

  public VideoRoomPluginEventData(String plugin) {
    super(plugin);
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public String getDisplay() {
    return display;
  }

  public void setDisplay(String display) {
    this.display = display;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getBitrate() {
    return bitrate;
  }

  public void setBitrate(long bitrate) {
    this.bitrate = bitrate;
  }

  public long getPrivate_id() {
    return private_id;
  }

  public void setPrivate_id(long private_id) {
    this.private_id = private_id;
  }

  public VideoRoomPluginEventDataStream[] getStream() {
    return stream;
  }

  public void setStream(VideoRoomPluginEventDataStream[] stream) {
    this.stream = stream;
  }
}
