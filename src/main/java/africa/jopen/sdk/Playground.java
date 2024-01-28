package africa.jopen.sdk;

import africa.jopen.sdk.events.JanusEventsEmissions;
import africa.jopen.sdk.models.MySqlConfiguration;
import africa.jopen.sdk.mysql.DBAccess;

public class Playground {
	
	
	public static void main( String[] args ) {
		
		Janus.DB_ACCESS =(new MySqlConfiguration("localhost", 3308, "janus_db","root","rootuser"));
		DBAccess.getInstance(Janus.DB_ACCESS);
		JanusEventsEmissions emissionsMock = new JanusEventsEmissions() {
			@Override
			public void onParticipantJoined( long participantId, String participantDisplay, String roomId ) {
				System.out.println("Participant joined" + participantId + " " + participantDisplay + " " + roomId);
			}
			
			@Override
			public void onParticipantLeft( long participantId, String participantDisplay, String roomId ) {
				System.out.println("Participant left" + participantId + " " + participantDisplay + " " + roomId);
			}
			
			@Override
			public void onRoomSessionStarted( String roomId, long firstParticipantId, String firstParticipantDisplay ) {
				System.out.println("Room session started" + firstParticipantId + " " + firstParticipantDisplay);
			}
			
			@Override
			public void onRoomSessionEnded( String roomId ) {
				System.out.println("Room session ended");
			}
		};
		
		
		String event = """
                [
                    {
                       "emitter": "MyJanusInstance",
                       "type": 256,
                       "subtype": 1,
                       "timestamp": 1705879077642460,
                       "event": {
                          "status": "update",
                          "info": {
                             "sessions": 2,
                             "handles": 4,
                             "peerconnections": 4,
                             "stats-period": 1
                          }
                       }
                    },
                    {
                           "emitter": "MyJanusInstance",
                           "type": 256,
                           "subtype": 1,
                           "timestamp": 1705879017642365,
                           "event": {
                              "status": "update",
                              "info": {
                                 "sessions": 0,
                                 "handles": 0,
                                 "peerconnections": 0,
                                 "stats-period": 1
                              }
                           }
                        },
                         {
                               "emitter": "MyJanusInstance",
                               "type": 16,
                               "subtype": 3,
                               "timestamp": 1705879036830185,
                               "session_id": 7198949627396591,
                               "handle_id": 4947903859375804,
                               "opaque_id": "videoroomtest-ihrfLaK2eabM",
                               "event": {
                                  "remote-candidate": "1191289763 1 udp 2122260223 172.24.0.1 52827 typ host generation 0 ufrag ik1d network-id 1",
                                  "stream_id": 1,
                                  "component_id": 1
                               }
                            }
                 ]
                """;
        /*JSONArray  jsonArray = SdkUtils.isJsonArray(event) ? new JSONArray(event) : new JSONArray().put(new JSONObject(event));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonEvent = jsonArray.getJSONObject(i);
            JanusEventsFactory janusEventsFactory = new JanusEventsFactory(jsonEvent, emissionsMock);
            janusEventsFactory.processEvent256();
        }*/
		emissionsMock.consumeEventAsync(event);
		
		System.out.println("Hello World");
		
	}
}
