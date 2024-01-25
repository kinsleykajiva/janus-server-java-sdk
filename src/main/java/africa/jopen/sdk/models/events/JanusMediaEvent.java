package africa.jopen.sdk.models.events;


import java.sql.Timestamp;

public class JanusMediaEvent {
	
	/**
	 * The root record for a Janus media event, containing information about the emitter,
	 * event type, subtype, timestamp, session ID, handle ID, opaque ID, and the nested event details.
	 */
	public record Root(String emitter, int type, int subtype, long timestamp, long session_id, long handle_id, String opaque_id, JanusMediaEvent.Event event) {
	}
	
	/**
	 * The record representing a media event within Janus, including details such as MID (Media ID),
	 * media index, codec information, base values, round-trip time (RTT), packet loss, jitter,
	 * link quality, and other relevant statistics.
	 */
	public record Event(String mid, int mindex, String media, String codec, int base, int rtt, int lost,
	                    int lost_by_remote, int jitter_local, int jitter_remote, int in_link_quality,
	                    int in_media_link_quality, int out_link_quality, int out_media_link_quality,
	                    int packets_received, int packets_sent, int bytes_received, int bytes_sent,
	                    int bytes_received_lastsec, int bytes_sent_lastsec, int nacks_received, int nacks_sent,
	                    int retransmissions_received) {
	}
	

	
	public String trackInsert(JanusMediaEvent.Root root){
		return "INSERT INTO janus_stats (session, handle, medium, base, lsr, lostlocal, lostremote, jitterlocal, jitterremote, packetssent, packetsrecv, bytessent, bytesrecv, nackssent, nacksrecv, timestamp) VALUES ("
				+ root.session_id() + ", "
				+ root.handle_id() + ", '"
				+ root.event.media() + "', "
				+ root.event.base() + ", "
				+ root.event.rtt() + ", "
				+ root.event.lost() + ", "
				+ root.event.lost_by_remote() + ", "
				+ root.event.jitter_local() + ", "
				+ root.event.jitter_remote() + ", "
				+ root.event.packets_sent() + ", "
				+ root.event.packets_received() + ", "
				+ root.event.bytes_sent() + ", "
				+ root.event.bytes_received() + ", "
				+ root.event.nacks_sent() + ", "
				+ root.event.nacks_received() + ", '"
				+ new Timestamp(root.timestamp()) + "');";
	}
}

