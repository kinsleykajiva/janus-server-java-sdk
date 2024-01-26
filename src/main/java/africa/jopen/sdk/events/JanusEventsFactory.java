package africa.jopen.sdk.events;

import africa.jopen.sdk.Janus;
import africa.jopen.sdk.models.events.*;
import africa.jopen.sdk.mysql.DBAccess;
import org.json.JSONObject;

public class JanusEventsFactory {
	
	private JSONObject jsonEvent;
	private JanusEventsEmissions emissions;
	
	public JanusEventsFactory( JSONObject jsonEvent ,JanusEventsEmissions emissions) {
		this.jsonEvent = jsonEvent;
		this.emissions = emissions;
	}
	
	public void processEvent16() {
		var jsonEventObj = jsonEvent.getJSONObject("event");
		var jevent = new JanusWebRTCStateEvent.Event(
				jsonEventObj.optString("ice", null),
				jsonEventObj.optInt("stream_id", 0),
				jsonEventObj.optInt("component_id", 0),
				jsonEventObj.optString("local-candidate", null),
				jsonEventObj.optString("remote-candidate", null)
		);
		var janusEvent = new JanusWebRTCStateEvent.Root(
				jsonEventObj.optString("emitter", null),
				jsonEventObj.optInt("type", 0),
				jsonEventObj.optInt("subtype", 0),
				jsonEventObj.optLong("timestamp", 0),
				jsonEventObj.optLong("session_id", 0),
				jsonEventObj.optLong("handle_id", 0),
				jsonEventObj.optString("opaque_id", null),
				jevent
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusWebRTCStateEvent().trackInsert(janusEvent);
			DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
		}
		
	}
	
	public void processEvent2(  ) {
		var jsonEventObj = jsonEvent.getJSONObject("event");
		var jevent = new JanusHandleEvent.Event(
				jsonEventObj.optString("name", null),
				jsonEventObj.optString("plugin", null),
				jsonEventObj.optString("opaque_id", null)
		);
		var janusEvent = new JanusHandleEvent.Root(
				jsonEventObj.optString("emitter", null),
				jsonEventObj.optInt("type", 0),
				jsonEventObj.optLong("timestamp", 0),
				jsonEventObj.optLong("session_id", 0),
				jsonEventObj.optLong("handle_id", 0),
				jsonEventObj.optString("opaque_id", null),
				jevent
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusHandleEvent().trackInsert(janusEvent);
			DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
		}
	}
	
	public void processEvent128() {
		var jsonEventObj = jsonEvent.getJSONObject("event");
		var jevent = new JanusTransportOriginatedEvent.Event(
				jsonEventObj.optString("transport", null),
				jsonEventObj.optString("id", null),
				new JanusTransportOriginatedEvent.Data(
						jsonEventObj.optString("event", null),
						jsonEventObj.optBoolean("admin_api", false),
						jsonEventObj.optString("ip", null),
						jsonEventObj.optInt("port", 0)
				)
		);
		var janusEvent = new JanusTransportOriginatedEvent.Root(
				jsonEventObj.optString("emitter", null),
				jsonEventObj.optInt("type", 0),
				jsonEventObj.optLong("timestamp", 0),
				jevent
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusTransportOriginatedEvent().trackInsert(janusEvent);
			DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
		}
	}
	
	public void processEvent32(  ) {
		var jsonEventObj = jsonEvent.getJSONObject("event");
		var jevent = new JanusMediaEvent.Event(
				jsonEventObj.optString("mid", null),
				jsonEventObj.optBoolean("receiving", false),
				jsonEventObj.optInt("receiving", 0),
				jsonEventObj.optString("media", null),
				jsonEventObj.optString("codec", null),
				jsonEventObj.optInt("base", 0),
				jsonEventObj.optInt("rtt", 0),
				jsonEventObj.optInt("lost", 0),
				jsonEventObj.optInt("lost_by_remote", 0),
				jsonEventObj.optInt("jitter_local", 0),
				jsonEventObj.optInt("jitter_remote", 0),
				jsonEventObj.optInt("in_link_quality", 0),
				jsonEventObj.optInt("in_media_link_quality", 0),
				jsonEventObj.optInt("out_link_quality", 0),
				jsonEventObj.optInt("out_media_link_quality", 0),
				jsonEventObj.optInt("packets_received", 0),
				jsonEventObj.optInt("packets_sent", 0),
				jsonEventObj.optInt("bytes_received", 0),
				jsonEventObj.optInt("bytes_sent", 0),
				jsonEventObj.optInt("bytes_received_lastsec", 0),
				jsonEventObj.optInt("bytes_sent_lastsec", 0),
				jsonEventObj.optInt("nacks_received", 0),
				jsonEventObj.optInt("nacks_sent", 0),
				jsonEventObj.optInt("retransmissions_received", 0)
		
		);
		var janusEvent = new JanusMediaEvent.Root(
				jsonEvent.getString("emitter"),
				jsonEvent.getInt("type"),
				jsonEvent.getInt("subtype"),
				jsonEvent.getLong("timestamp"),
				jsonEvent.getLong("session_id"),
				jsonEvent.getLong("handle_id"),
				jsonEvent.getString("opaque_id"),
				jevent
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusMediaEvent().trackInsert(janusEvent);
			DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
		}
	}
	
	public void processEvent8( ) {
		var jsep = jsonEvent.getJSONObject("event").has("jsep") ? new JanusJSEPEvent.Jsep(jsonEvent.getJSONObject("event").getJSONObject("jsep").optString("type", null),
				jsonEvent.getJSONObject("event").getJSONObject("jsep").optString("sdp", null)) : null;
		var janusJSEPEventEvent = new JanusJSEPEvent.Event(jsonEvent.getString("name"), jsep);
		var janusEvent = new JanusJSEPEvent.Root(
				jsonEvent.optString("emitter", null),
				jsonEvent.optInt("type", 0),
				jsonEvent.optLong("timestamp", 0),
				jsonEvent.optLong("session_id", 0),
				jsonEvent.optLong("handle_id", 0),
				jsonEvent.optString("opaque_id", null),
				janusJSEPEventEvent
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusJSEPEvent().trackInsert(janusEvent);
			DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
		}
	}
	
	public void processEvent1( ) {
		var janusEvent = new JanusSessionEvent.Root(
				jsonEvent.optString("emitter", null),
				jsonEvent.optInt("type", 0),
				jsonEvent.optLong("timestamp", 0),
				jsonEvent.optLong("session_id", 0),
				new JanusSessionEvent.Event(
						jsonEvent.getString("name"),
						jsonEvent.has("transport") ? new JanusSessionEvent.Transport(
								jsonEvent.getJSONObject("transport").optString("transport", null),
								jsonEvent.getJSONObject("transport").optLong("id", 0)
						) : null
				)
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusSessionEvent().trackInsert(janusEvent);
			DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
		}
	}
	
	public void processEvent256( ) {
		var jsonEventObj = jsonEvent.getJSONObject("event");
		var jevent = new JanusCoreEvent.Event(
				jsonEventObj.optString("status", null),
				new JanusCoreEvent.Info(
						jsonEventObj.optLong("sessions", 0),
						jsonEventObj.optLong("handles", 0),
						jsonEventObj.optLong("peerconnections", 0),
						jsonEventObj.optLong("stats-period", 0)
				)
		);
		var janusEvent = new JanusCoreEvent.Root(
				jsonEventObj.optString("emitter", null),
				jsonEventObj.optInt("type", 0),
				jsonEventObj.optInt("subtype", 0),
				jsonEventObj.optLong("timestamp", 0),
				jevent
		);
		if (Janus.DB_ACCESS != null) {
			var insertSEL = new JanusCoreEvent().trackInsert(janusEvent);
			DBAccess.getInstance(Janus.DB_ACCESS).SQLBatchExec(insertSEL);
		}
	}
	
	
}
