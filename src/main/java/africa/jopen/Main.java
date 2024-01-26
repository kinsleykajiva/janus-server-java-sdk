package africa.jopen;


import africa.jopen.sdk.Janus;
import africa.jopen.sdk.SdkUtils;
import africa.jopen.sdk.events.JanusEventsEmissions;
import africa.jopen.sdk.models.JanusConfiguration;
import africa.jopen.sdk.models.MySqlConfiguration;
import africa.jopen.sdk.mysql.DBAccess;

public class Main implements JanusEventsEmissions {
	static final String JANUS_URL = "https://visioconf.qalqul.io/janus";
	static String JANUS_ADMIN_SECRET= "JYL8UqUe9XLacMe8F2MGs46H68vv4WGX";
	public static void main( String[] args ) {
		
		Janus.DB_ACCESS =(new MySqlConfiguration("localhost", 3308, "janus_db","root","rootuser"));
		Janus janus = new Janus(true,new JanusConfiguration(JANUS_URL,"","",JANUS_ADMIN_SECRET));
		/*var result = janus.janusRestApiClient.janusVideoRoomPlugInAPI.checkIfVideoRoomExists("1234");
		var result1 = janus.janusRestApiClient.janusVideoRoomPlugInAPI.checkIfVideoRoomExistsBoolCheck("1234");
		System.out.println("result = " + result);
		System.out.println("result = " + result1);*/
		
		/*var result3 = janus.janusRestApiClient.janusVideoRoomPlugInAPI.createJanusRoom("91","room",
				"1234","1234",5,true,true,"/tmp");
		System.out.println("Create result =   " + result3);*/
		
		/*var result34 = janus.janusRestApiClient.janusVideoRoomPlugInAPI.deleteRoom("91","1234");
		System.out.println("Create result =   " + result34);*/
		/*var result341 = janus.janusRestApiClient.janusVideoRoomPlugInAPI.getRooms();
		System.out.println("list  result =   " + result341);*/
		var result3411 = janus.janusRestApiClient.janusVideoRoomPlugInAPI.listRoomParticipants("1234");
		System.out.println("list  result =   " + result3411);
		
		// consumeEventAsync(janus);
		
	}
	
	@Override
	public void onParticipantJoined( long participantId, String participantDisplay, String roomId ) {
		System.out.println("onParticipantJoined event = " + participantId + " " + participantDisplay + " " + roomId);
	}
	
	@Override
	public void onParticipantLeft( long participantId, String participantDisplay, String roomId ) {
		System.out.println("onParticipantLeft event = " + participantId + " " + participantDisplay + " " + roomId);
	}
	
	@Override
	public void onRoomSessionStarted( String roomId, long firstParticipantId, String firstParticipantDisplay ) {
		System.out.println("onRoomSessionStarted event = " + roomId + " " + firstParticipantId + " " + firstParticipantDisplay);
	}
	
	@Override
	public void onRoomSessionEnded( String roomId ) {
		System.out.println("onRoomSessionEnded event = " + roomId);
	}
}