package io.github.kinsleykajiva.janus;

public class JanusUtils {
	
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
	
}
