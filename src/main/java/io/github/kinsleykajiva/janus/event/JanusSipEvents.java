package io.github.kinsleykajiva.janus.event;

import java.util.Objects;

public class JanusSipEvents {
	public static abstract class RegistrationEvent {
	    private final String event;
	
	    protected RegistrationEvent(String event) {
	        this.event = event;
	    }
	
	    public String event() {
	        return event;
	    }
	}
	
	public static final class SuccessfulRegistration extends RegistrationEvent {
		private final String event;
		private final Long masterId;
		private final String username;
		
		public SuccessfulRegistration(String event, Long masterId, String username) {
			super(event);
			this.event = event;
			this.masterId = masterId;
			this.username = username;
		}
		
		@Override
		public String event() {
			return event;
		}
		
		public Long masterId() {
			return masterId;
		}
		
		public String username() {
			return username;
		}
		
		
	}
	
	public static final class ErrorRegistration extends RegistrationEvent {
		private final String event;
		private final int code;
		private final String reason;
		
		public ErrorRegistration(String event, int code, String reason) {
			super(event);
			this.event = event;
			this.code = code;
			this.reason = reason;
		}
		
		@Override
		public String event() {
			return event;
		}
		
		public int code() {
			return code;
		}
		
		public String reason() {
			return reason;
		}
		
		
	}
	
}
