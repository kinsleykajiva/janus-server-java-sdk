package africa.jopen.sdk.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the configuration for the Janus server.
 *
 * @param url         The URL of the Janus server.
 * @param apiSecret   The API secret for authentication (optional).
 * @param adminKey    The admin key for authentication (optional).
 * @param adminSecret The admin secret for authentication (optional).
 */
public record JanusConfiguration(@NotNull String url, @Nullable String apiSecret, @Nullable String adminKey, @Nullable String adminSecret) {
}
