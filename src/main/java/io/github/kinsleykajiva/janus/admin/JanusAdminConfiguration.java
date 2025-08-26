package io.github.kinsleykajiva.janus.admin;

import java.net.URI;

/**
 * Configuration for the Janus Admin API client.
 *
 * @param uri          The URI of the Janus Admin WebSocket endpoint.
 * @param adminSecret  The secret required to authenticate with the Admin API.
 */
public record JanusAdminConfiguration(URI uri, String adminSecret) {}
