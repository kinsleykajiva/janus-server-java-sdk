package africa.jopen;


import africa.jopen.sdk.Janus;
import africa.jopen.sdk.models.JanusConfiguration;

public class Main {
	static final String JANUS_URL = "https://.io/janus";
	static String JANUS_ADMIN_SECRET= "JYWGX";
	public static void main( String[] args ) {
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
		
		
	}
}