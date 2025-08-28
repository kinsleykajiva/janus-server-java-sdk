package io.github.kinsleykajiva.janus.utils;

import io.github.kinsleykajiva.janus.admin.JanusAdminClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JanusUtils {
	private static final Logger logger = LoggerFactory.getLogger(JanusUtils.class);
	public enum SipHoldDirection {
		// Indicates the SIP endpoint will only send media (RFC 3264, SDP "a=sendonly")
		// See Janus docs: https://janus.conf.meetecho.com/docs.html#siphold
		SENDONLY("sendonly"),
		// Indicates the SIP endpoint will only receive media (RFC 3264, SDP "a=recvonly")
		// See Janus docs: https://janus.conf.meetecho.com/docs.html#siphold
		RECVONLY("recvonly"),
		// Indicates the SIP endpoint will neither send nor receive media (RFC 3264, SDP "a=inactive")
		// See Janus docs: https://janus.conf.meetecho.com/docs.html#siphold
		INACTIVE("inactive");
	
		private final String value;
	
		SipHoldDirection(String value) {
			this.value = value;
		}
	
		public String getValue() {
			return value;
		}
	}
	
	/**
	 * Validates whether the given address is a valid IP address or domain name.
	 * Removes any leading "http://" or "https://" before validation.
	 *
	 * @param address The address to validate.
	 * @throws IllegalArgumentException if the address is not a valid IP or domain.
	 */
	public static void validateIpOrDomain(String address) {
	    if (address == null || address.isEmpty()) {
	        throw new IllegalArgumentException("Address cannot be null or empty");
	    }
	    // Remove protocol if present
	    address = address.replaceFirst("^(https?://)", "");
	
	    // Regex for IPv4
	    String ipv4Pattern = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
	    // Regex for domain name
	    String domainPattern = "^(?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.(?:[A-Za-z]{2,6}|[A-Za-z0-9-]{2,})$|^(?!-)[A-Za-z0-9-]{1,63}(?<!-)$";
	
	    if (address.matches(ipv4Pattern) || address.matches(domainPattern)) {
	        return; // Valid
	    }
	    throw new IllegalArgumentException("Invalid IP address or domain name: " + address);
	}
	
	public static ServerInfo convertToServerInfo(JSONObject response) {
		try {
			JSONObject dependenciesJson = response.optJSONObject("dependencies", new JSONObject());
			java.util.Map<String, ServerInfo.Dependency> dependencies = dependenciesJson.keySet().stream()
					                                                            .collect(java.util.stream.Collectors.toMap(
							                                                            key -> key,
							                                                            key -> new ServerInfo.Dependency(dependenciesJson.getString(key))
					                                                            ));
			
			JSONObject transportsJson = response.optJSONObject("transports", new JSONObject());
			java.util.Map<String, ServerInfo.Transport> transports = transportsJson.keySet().stream()
					                                                         .collect(java.util.stream.Collectors.toMap(
							                                                         key -> key,
							                                                         key -> {
								                                                         JSONObject t = transportsJson.getJSONObject(key);
								                                                         return new ServerInfo.Transport(
										                                                         t.getString("name"),
										                                                         t.getString("author"),
										                                                         t.getString("description"),
										                                                         t.getString("version_string"),
										                                                         t.getInt("version")
								                                                         );
							                                                         }
					                                                         ));
			
			JSONObject pluginsJson = response.optJSONObject("plugins", new JSONObject());
			java.util.Map<String, ServerInfo.Plugin> plugins = pluginsJson.keySet().stream()
					                                                   .collect(java.util.stream.Collectors.toMap(
							                                                   key -> key,
							                                                   key -> {
								                                                   JSONObject p = pluginsJson.getJSONObject(key);
								                                                   return new ServerInfo.Plugin(
										                                                   p.getString("name"),
										                                                   p.getString("author"),
										                                                   p.getString("description"),
										                                                   p.getString("version_string"),
										                                                   p.getInt("version")
								                                                   );
							                                                   }
					                                                   ));
			
			return new ServerInfo(
					response.getString("janus"),
					response.getString("name"),
					response.getInt("version"),
					response.getString("version_string"),
					response.getString("author"),
					response.optString("commit-hash", ""),
					response.optString("compile-time", ""),
					response.getBoolean("log-to-stdout"),
					response.getBoolean("log-to-file"),
					response.getBoolean("data_channels"),
					response.getBoolean("accepting-new-sessions"),
					response.getInt("session-timeout"),
					response.getInt("reclaim-session-timeout"),
					response.getInt("candidates-timeout"),
					response.optString("server-name", ""),
					response.optString("local-ip", ""),
					response.optString("public-ip", ""),
					response.getBoolean("ipv6"),
					response.getBoolean("ice-lite"),
					response.getBoolean("ice-tcp"),
					response.optString("ice-nomination", ""),
					response.getBoolean("ice-consent-freshness"),
					response.getBoolean("ice-keepalive-conncheck"),
					response.getBoolean("hangup-on-failed"),
					response.getBoolean("full-trickle"),
					response.getBoolean("mdns-enabled"),
					response.getInt("min-nack-queue"),
					response.getBoolean("nack-optimizations"),
					response.getInt("twcc-period"),
					response.getInt("dtls-mtu"),
					response.getInt("static-event-loops"),
					response.getBoolean("api_secret"),
					response.getBoolean("auth_token"),
					response.getBoolean("event_handlers"),
					response.getBoolean("opaqueid_in_api"),
					dependencies,
					transports,
					plugins
			);
		} catch (JSONException e) {
			logger.error("Failed to parse server info response: {}", e.getMessage(), e);
			throw new JanusException("Invalid server info response format", e);
		}
	}
	
}
