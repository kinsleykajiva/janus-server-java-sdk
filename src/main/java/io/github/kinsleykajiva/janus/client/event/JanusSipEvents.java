package io.github.kinsleykajiva.janus.client.event;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
		private final Long masterId;
		private final String username;
		
		public SuccessfulRegistration(String event, Long masterId, String username) {
			super(event);
			this.masterId = masterId;
			this.username = username;
		}
		
		public Long masterId() {
			return masterId;
		}
		
		public String username() {
			return username;
		}
	}
	
	public static final class ErrorRegistration extends RegistrationEvent {
		private final int code;
		private final String reason;
		
		public ErrorRegistration(String event, int code, String reason) {
			super(event);
			this.code = code;
			this.reason = reason;
		}
		
		public int code() {
			return code;
		}
		
		public String reason() {
			return reason;
		}
	}
	public record  TransferEvent(String refer_id, String refer_to, String referred_by, String replaces , org.json.JSONObject headers){}
	public record  NotifyEvent(String notify_, String substate, String contentType, String content , org.json.JSONObject headers){}
	public record  InfoEvent(String sender, String displayname, String type, String content , org.json.JSONObject headers){}
	public record  MessageDeliveryEvent(String call_id,int code,String reason ){}
	public record  MessageEvent(String sender, String displayName, String content_type, String content, org.json.JSONObject headers ){}
	public record  MissedCallEvent(String caller,String displayName,String callee ){}
	public record  HangupEvent(int code, @NonNull  String reason,@NonNull String callId ){}
	public record InComingCallEvent(@NonNull String username, @NonNull  String callId, @NonNull  String displayName, @NonNull String callee, @Nullable JanusJsep jsep ){}
	
}
