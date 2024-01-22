package africa.jopen.sdk.models.events;

public class VideoRoomPluginEventDataStream {
	private String type, codec;
	private int mindex = 0, mid = 0;
	
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
