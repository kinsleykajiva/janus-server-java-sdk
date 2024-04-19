package io.github.kinsleykajiva.utils;

import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface JanusEventHandler {
@NonBlocking
void handleEvent( @NotNull JSONObject event);

@NonBlocking void onConnected();
}
