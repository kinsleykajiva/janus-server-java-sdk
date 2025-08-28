package io.github.kinsleykajiva.janus.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY;

/**
 * A configuration class for the JanusClient.
 * It provides multiple constructors for flexible setup of the Janus Gateway WebSocket URL
 * and allows for programmatic control over the SDK's logging.
 */
public class JanusConfiguration {
	
	private final URI uri;
	private final boolean logEnabled;
	
	/**
	 * The most direct constructor, taking a full WebSocket URL.
	 *
	 * @param websocketUrl The complete WebSocket URL (e.g., "ws://localhost:8188/janus" or "wss://your.domain.com/janus"). Must not be null.
	 * @param logEnabled   If true, enables the default SLF4J simple logger at the INFO level. If false, disables it.
	 * @throws IllegalArgumentException if the websocketUrl is not a valid URI.
	 */
	public JanusConfiguration(String websocketUrl, boolean logEnabled) {
		Objects.requireNonNull(websocketUrl, "WebSocket URL cannot be null");
		try {
			this.uri = new URI(websocketUrl);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid WebSocket URL provided: " + websocketUrl, e);
		}
		this.logEnabled = logEnabled;
		
		// Programmatically configure the SLF4J SimpleLogger shipped with the SDK.
		// A user can override this by providing their own SLF4J implementation (e.g., Logback).
		if (logEnabled) {
			System.setProperty(DEFAULT_LOG_LEVEL_KEY, "INFO");
		} else {
			System.setProperty(DEFAULT_LOG_LEVEL_KEY, "OFF");
		}
	}
	
	/**
	 * A helper constructor to build the WebSocket URL from its components.
	 *
	 * @param host       The hostname or IP address of the Janus server (e.g., "localhost").
	 * @param port       The port number (e.g., 8188 for ws, 8989 for wss).
	 * @param path       The path for the Janus endpoint (usually "/janus").
	 * @param useWss     If true, uses the "wss://" protocol for a secure connection. If false, uses "ws://".
	 * @param logEnabled If true, enables logging.
	 */
	public JanusConfiguration(String host, int port, String path, boolean useWss, boolean logEnabled) {
		this(buildUrl(host, port, path, useWss), logEnabled);
	}
	
	/**
	 * A minimal constructor for connecting to a local Janus instance with default settings.
	 * <p>
	 * Defaults to:
	 * <ul>
	 *   <li>Port: 8188</li>
	 *   <li>Path: "/janus"</li>
	 *   <li>Secure Connection (WSS): false</li>
	 *   <li>Logging: enabled</li>
	 * </ul>
	 *
	 * @param host The hostname or IP address of the Janus server (e.g., "localhost").
	 */
	public JanusConfiguration(String host) {
		this(host, 8188, "/janus", false, true);
	}
	
	/**
	 * Helper method to construct a valid WebSocket URL string from its parts.
	 */
	private static String buildUrl(String host, int port, String path, boolean useWss) {
		Objects.requireNonNull(host, "Host cannot be null");
		Objects.requireNonNull(path, "Path cannot be null");
		
		String scheme = useWss ? "wss" : "ws";
		// Ensure the path starts with a slash for correctness.
		String cleanPath = path.startsWith("/") ? path : "/" + path;
		
		return String.format("%s://%s:%d%s", scheme, host, port, cleanPath);
	}
	
	/**
	 * Returns the configured WebSocket URI.
	 * @return The {@link URI} for the Janus Gateway.
	 */
	public URI getUri() {
		return uri;
	}
	
	/**
	 * Returns whether logging is enabled.
	 * @return true if logging is enabled, false otherwise.
	 */
	public boolean isLogEnabled() {
		return logEnabled;
	}
}