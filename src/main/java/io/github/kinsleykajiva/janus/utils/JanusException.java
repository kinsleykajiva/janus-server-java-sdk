package io.github.kinsleykajiva.janus.utils;

public class JanusException extends RuntimeException {
	public JanusException(String message) {
		super(message);
	}
	
	public JanusException(String message, Throwable cause) {
		super(message, cause);
	}
}