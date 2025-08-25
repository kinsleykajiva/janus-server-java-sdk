package io.github.kinsleykajiva.janus.event;



public interface JanusSipEventListener extends JanusEventListener {
	void onRegisteredEvent(JanusEvent event);
	void onIncomingCallEvent(JanusSipEvents.InComingCallEvent event);
	void onHangupCallEvent(JanusSipEvents.HangupEvent event);
}
