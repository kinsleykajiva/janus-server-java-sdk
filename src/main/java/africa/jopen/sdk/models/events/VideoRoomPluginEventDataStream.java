package africa.jopen.sdk.models.events;
public record VideoRoomPluginEventDataStream(String type, String codec, String feed_display, String feed_mid, int mindex, int mid, boolean ready, boolean send, long feed_id) {}