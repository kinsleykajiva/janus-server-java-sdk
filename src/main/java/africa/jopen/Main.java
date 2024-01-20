package africa.jopen;


import africa.jopen.sdk.Janus;
import africa.jopen.sdk.JanusPlugins;
import africa.jopen.sdk.models.JanusConfiguration;

public class Main {
	static final String JANUS_URL = "wss://janus.conf.meetecho.com/ws";
	public static void main( String[] args ) {
		Janus janus = new Janus(true,new JanusConfiguration(JANUS_URL,"","",""));
		var result = janus.janusRestApiClient.checkIfVideoRoomExists(JanusPlugins.JANUS_VIDEO_ROOM,"1234");
		System.out.println("result = " + result);
	}
}