package io.github.kinsleykajiva.janus.client.event;

@FunctionalInterface
public interface JanusEventListener {
	void onEvent(JanusEvent event);
}