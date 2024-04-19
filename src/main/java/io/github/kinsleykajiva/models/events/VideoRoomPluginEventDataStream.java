package io.github.kinsleykajiva.models.events;

public class VideoRoomPluginEventDataStream {
	private String type, codec,feed_display,feed_mid;
	private int mindex = 0, mid = 0;
	private  boolean ready,send;private long feed_id;
	
	public String getFeed_display() {
		return feed_display;
	}
	
	public void setFeed_display( String feed_display ) {
		this.feed_display = feed_display;
	}
	
	public String getFeed_mid() {
		return feed_mid;
	}
	
	public void setFeed_mid( String feed_mid ) {
		this.feed_mid = feed_mid;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public void setReady( boolean ready ) {
		this.ready = ready;
	}
	
	public boolean isSend() {
		return send;
	}
	
	public void setSend( boolean send ) {
		this.send = send;
	}
	
	public long getFeed_id() {
		return feed_id;
	}
	
	public void setFeed_id( long feed_id ) {
		this.feed_id = feed_id;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType( String type ) {
		this.type = type;
	}
	
	public String getCodec() {
		return codec;
	}
	
	public void setCodec( String codec ) {
		this.codec = codec;
	}
	
	public int getMindex() {
		return mindex;
	}
	
	public void setMindex( int mindex ) {
		this.mindex = mindex;
	}
	
	public int getMid() {
		return mid;
	}
	
	public void setMid( int mid ) {
		this.mid = mid;
	}
}
