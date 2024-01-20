package africa.jopen.sdk.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record JanusConfiguration(@NotNull String url, @Nullable String apiSecret, @Nullable String adminKey, @Nullable String adminSecret) {
}
