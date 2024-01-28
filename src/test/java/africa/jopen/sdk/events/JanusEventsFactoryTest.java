package africa.jopen.sdk.events;

import africa.jopen.sdk.Janus;
import africa.jopen.sdk.SdkUtils;
import africa.jopen.sdk.models.MySqlConfiguration;
import africa.jopen.sdk.models.events.*;
import africa.jopen.sdk.mysql.DBAccess;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the JanusEventsFactory class, specifically the method processEvent256.
 */
public class JanusEventsFactoryTest {

    @Test
    public void processEvent256Test() {
        // Creating a mock object for JanusEventsEmissions
        JanusEventsEmissions emissionsMock = mock(JanusEventsEmissions.class);
        
        
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
       

        //assertNull(Janus.DB_ACCESS, "Expected Janus DB Access to be null");
       // assertEquals(expected, janusEventsFactory.processEvent256(), "Expected object did not match actual");
    }
}