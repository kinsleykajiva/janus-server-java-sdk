package io.github.kinsleykajiva.janus.event;

@FunctionalInterface
public interface JanusEventListener {
	void onEvent(JanusEvent event);
}