package io.github.kinsleykajiva.models.events;

import io.github.kinsleykajiva.Janus;
import io.github.kinsleykajiva.cache.DatabaseConnection;
import io.github.kinsleykajiva.cache.mongodb.MongoConnection;
import io.github.kinsleykajiva.cache.mysql.MySqlConnection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

import static io.github.kinsleykajiva.models.events.JanusCoreEvent.getDatabaseConnectionListMap;

/**
 * Represents a Janus media event, containing information about the emitter, event type, subtype,
 * timestamp, session ID, handle ID, opaque ID, and the nested event details.
 */
public class JanusMediaEvent {

  /**
   * The root record for a Janus media event, containing information about the emitter, event type,
   * subtype, timestamp, session ID, handle ID, opaque ID, and the nested event details.
   *
   * @param emitter The emitter of the Janus media event.
   * @param type The type of the Janus media event.
   * @param subtype The subtype of the Janus media event.
   * @param timestamp The timestamp of the Janus media event.
   * @param session_id The session ID of the Janus media event.
   * @param handle_id The handle ID of the Janus media event.
   * @param opaque_id The opaque ID of the Janus media event.
   * @param event The nested event details of the Janus media event.
   */
  public record Root(
      @NotNull String emitter,
      @NotNull Integer type,
      @NotNull Integer subtype,
      @NotNull Long timestamp,
      @NotNull Long session_id,
      @NotNull Long handle_id,
      @NotNull String opaque_id,
      @NotNull JanusMediaEvent.Event event) {}

  /**
   * The record representing a media event within Janus, including details such as MID (Media ID),
   * media index, codec information, base values, round-trip time (RTT), packet loss, jitter, link
   * quality, and other relevant statistics.
   *
   * @param mid The MID (Media ID) of the Janus media event.
   * @param receiving The receiving status of the Janus media event.
   * @param mindex The media index of the Janus media event.
   * @param media The media type of the Janus media event.
   * @param codec The codec information of the Janus media event.
   * @param base The base value of the Janus media event.
   * @param rtt The round-trip time (RTT) of the Janus media event.
   * @param lost The packet loss of the Janus media event.
   * @param lost_by_remote The packet loss by remote of the Janus media event.
   * @param jitter_local The jitter local of the Janus media event.
   * @param jitter_remote The jitter remote of the Janus media event.
   * @param in_link_quality The in link quality of the Janus media event.
   * @param in_media_link_quality The in media link quality of the Janus media event.
   * @param out_link_quality The out link quality of the Janus media event.
   * @param out_media_link_quality The out media link quality of the Janus media event.
   * @param packets_received The packets received of the Janus media event.
   * @param packets_sent The packets sent of the Janus media event.
   * @param bytes_received The bytes received of the Janus media event.
   * @param bytes_sent The bytes sent of the Janus media event.
   * @param bytes_received_lastsec The bytes received last second of the Janus media event.
   * @param bytes_sent_lastsec The bytes sent last second of the Janus media event.
   * @param nacks_received The NACKs received of the Janus media event.
   * @param nacks_sent The NACKs sent of the Janus media event.
   * @param retransmissions_received The retransmissions received of the Janus media event.
   */
  public record Event(
      @NotNull String mid,
      boolean receiving,
      Integer mindex,
      @NotNull String media,
      String codec,
      Integer base,
      Integer rtt,
      Integer lost,
      Integer lost_by_remote,
      Integer jitter_local,
      Integer jitter_remote,
      Integer in_link_quality,
      Integer in_media_link_quality,
      Integer out_link_quality,
      Integer out_media_link_quality,
      Integer packets_received,
      Integer packets_sent,
      Integer bytes_received,
      Integer bytes_sent,
      Integer bytes_received_lastsec,
      Integer bytes_sent_lastsec,
      Integer nacks_received,
      Integer nacks_sent,
      Integer retransmissions_received) {}

  /**
   * Constructs SQL insert statements for the janus_stats and janus_media tables using the provided
   * JanusMediaEvent.Root object.
   *
   * <p>Note: This method assumes that all fields in the JanusMediaEvent.Root object are non-null.
   * If any of the fields are null, the resulting SQL statement may be invalid.
   *
   * @param root The JanusMediaEvent.Root object containing the data to be inserted into the
   *     database.
   * @return A String containing two SQL insert statements: one for the janus_stats table and one
   *     for the janus_media table.
   */
  public Map<DatabaseConnection, List<String>> trackInsert(Root root) {
    Map<DatabaseConnection, List<String>> map = new HashMap<>();
    if (root == null) {
      return map;
    }
    var timestamp = new Timestamp(root.timestamp() / 1000);
    String receiving = root.event.receiving() ? "true" : "false";
    // ToDo! this needs to be review because it will be false positive as not all events will have
    // all these attributes even if they are set to default values of zero , null and false values
    // but yet this can or is harmless but just be aware if this affects your data integrity as two
    // inserts are made regardless
    var sql =
        "INSERT INTO janus_stats (session, handle, medium, base, lsr, lostlocal, lostremote, jitterlocal, jitterremote, packetssent, packetsrecv, bytessent, bytesrecv, nackssent, nacksrecv, timestamp) VALUES ("
            + root.session_id()
            + ", "
            + root.handle_id()
            + ", '"
            + root.event.media()
            + "', "
            + root.event.base()
            + ", "
            + root.event.rtt()
            + ", "
            + root.event.lost()
            + ", "
            + root.event.lost_by_remote()
            + ", "
            + root.event.jitter_local()
            + ", "
            + root.event.jitter_remote()
            + ", "
            + root.event.packets_sent()
            + ", "
            + root.event.packets_received()
            + ", "
            + root.event.bytes_sent()
            + ", "
            + root.event.bytes_received()
            + ", "
            + root.event.nacks_sent()
            + ", "
            + root.event.nacks_received()
            + ", '"
            + timestamp
            + "'); "
            + "INSERT INTO janus_media (session, handle, receiving, timestamp) VALUES ("
            + root.session_id()
            + ", "
            + root.handle_id()
            + ","
            + " '"
            + receiving
            + "', "
            + "'"
            + timestamp
            + "');";
    /*	var doc = String.format(
    		"{\"insert\": '%s', \"documents\": [{\"session\": %d, \"handle\": %d, \"medium\": \"%s', 'base': %d, 'lsr': %d, 'lostlocal': %d, 'lostremote': %d, 'jitterlocal': %d, 'jitterremote': %d, 'packetssent': %d, 'packetsrecv': %d, 'bytessent': %d, 'bytesrecv': %d, 'nackssent': %d, 'nacksrecv': %d, 'timestamp': '%s'}]}",
    		"janus_stats",
    		root.session_id(),
    		root.handle_id(),
    		root.event.media(),
    		root.event.base(),
    		root.event.rtt(),
    		root.event.lost(),
    		root.event.lost_by_remote(),
    		root.event.jitter_local(),
    		root.event.jitter_remote(),
    		root.event.packets_sent(),
    		root.event.packets_received(),
    		root.event.bytes_sent(),
    		root.event.bytes_received(),
    		root.event.nacks_sent(),
    		root.event.nacks_received(),
    		timestamp,
    		root.session_id(),
    		root.handle_id(),
    		receiving,
    		timestamp
    ); */
    var doc =
        String.format(
            "{\"insert\": \"%s\", \"documents\": [{\"session\": %d, \"handle\": %d, \"medium\": \"%s\", \"base\": %d, \"lsr\": %d, \"lostlocal\": %d, \"lostremote\": %d, \"jitterlocal\": %d, \"jitterremote\": %d, \"packetssent\": %d, \"packetsrecv\": %d, \"bytessent\": %d, \"bytesrecv\": %d, \"nackssent\": %d, \"nacksrecv\": %d, \"timestamp\": \"%s\"}]}",
            "janus_stats",
            root.session_id(),
            root.handle_id(),
            root.event.media(),
            root.event.base(),
            root.event.rtt(),
            root.event.lost(),
            root.event.lost_by_remote(),
            root.event.jitter_local(),
            root.event.jitter_remote(),
            root.event.packets_sent(),
            root.event.packets_received(),
            root.event.bytes_sent(),
            root.event.bytes_received(),
            root.event.nacks_sent(),
            root.event.nacks_received(),
            timestamp,
            root.session_id(),
            root.handle_id(),
            receiving,
            timestamp);
    
    return getDatabaseConnectionListMap(map, sql, doc);
  }
}
