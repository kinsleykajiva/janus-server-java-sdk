package io.github.kinsleykajiva.utils;

/**
 * This class contains several constants related to the Janus/Admin API like:
 * <p>
 * - Janus request names
 * <p>
 * - Janus response names
 * <p>
 * - Janus event names
 * <p>
 * - Janode event names
 * <p>
 * Some helper methods related to the protocols are defined here too.
 */
public class Protocol {
	
	
	/**
	 * Janus protocol constants
	 */
	public static class JANUS {
		public static final String JANUS        = "janus";
		public static final String SESSION_ID   = "session_id";
		public static final String TRANSACTION  = "transaction";
		public static final String HANDLE_ID    = "handle_id";
		public static final String PLUG_IN      = "plugin";
		public static final String ADMIN_KEY    = "admin_key";
		public static final String API_SECRET   = "apisecret";
		public static final String ADMIN_SECRET = "admin_secret";
		/**
		 * Janus API requests
		 */
		public static class REQUEST {
			/* connection level requests */
			public static final String SERVER_INFO     = "info";
			/* session level requests */
			public static final String CREATE_SESSION  = "create";
			public static final String KEEPALIVE       = "keepalive";
			public static final String DESTROY_SESSION = "destroy";
			/* handle level requests */
			public static final String ATTACH_PLUGIN   = "attach";
			public static final String MESSAGE         = "message";
			public static final String TRICKLE         = "trickle";
			public static final String HANGUP          = "hangup";
			public static final String DETACH_PLUGIN   = "detach";
		}
		
		
		/**
		 * Janus temporary response (ack)
		 */
		public static final String ACK = "ack";
		
		/**
		 * Janus definitive responses
		 */
		public static class RESPONSE {
			public static final String SUCCESS     = "success";
			public static final String SERVER_INFO = "server_info";
			public static final String ERROR       = "error";
		}
		
		/**
		 * Janus events
		 */
		public static class EVENT {
			public static final String EVENT      = "event";
			public static final String DETACHED   = "detached";
			public static final String ICE_FAILED = "ice-failed";
			public static final String HANGUP     = "hangup";
			public static final String MEDIA      = "media";
			public static final String TIMEOUT    = "timeout";
			public static final String WEBRTCUP   = "webrtcup";
			public static final String SLOWLINK   = "slowlink";
			public static final String TRICKLE    = "trickle";
			
		}
		
		/**
		 * Janus Admin API requests
		 */
		public static class ADMIN {
			public static final String LIST_SESSIONS = "list_sessions";
			public static final String LIST_HANDLES  = "list_handles";
			public static final String HANDLE_INFO   = "handle_info";
			public static final String START_PCAP    = "start_pcap";
			public static final String STOP_PCAP     = "stop_pcap";
		}
	}
	
	
}
