package io.github.kinsleykajiva.janus.event;


/**
 * A listener for SIP-specific events in the Janus server.
 * Extends the generic {@link JanusEventListener}.
 * Provides default implementations for specific event handlers, allowing developers to
 * implement only the events they are interested in.
 */
public interface JanusSipEventListener extends JanusEventListener {

	/**
	 * Fired when a 'registered' event is received from the SIP plugin.
	 * This can indicate both successful and failed registration attempts.
	 *
	 * @param event The generic Janus event containing the full payload.
	 */
	default void onRegisteredEvent(JanusSipEvents.SuccessfulRegistration event) {
	}
	
	default void onFailedRegistrationEvent(JanusSipEvents.ErrorRegistration event) {
	}

	/**
	 * Fired when an 'incomingcall' event is received.
	 *
	 * @param event The specific event object containing details about the incoming call.
	 */
	default void onIncomingCallEvent(JanusSipEvents.InComingCallEvent event) {
	}

	/**
	 * Fired when a 'hangup' event is received.
	 *
	 * @param event The specific event object containing details about the hangup.
	 */
	default void onHangupCallEvent(JanusSipEvents.HangupEvent event) {
	}
	
}
