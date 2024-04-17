package io.github.kinsleykajiva;

public enum JanusPlugins {
	JANUS_AUDIO_BRIDGE("janus.plugin.audiobridge"),
	JANUS_RECORD_PLAY("janus.plugin.recordplay"),
	JANUS_STREAMING("janus.plugin.streaming"),
	JANUS_SIP("janus.plugin.sip"),
	
	JANUS_VIDEO_ROOM("janus.plugin.videoroom"),
	JANUS_VOICE_MAIL("janus.plugin.voicemail");
	private final String plugin_name;
	@Override
	public String toString() {
		return plugin_name;
	}
	
	private boolean EqualsString(String str) {
		return plugin_name.equals(str);
	}
	
	private JanusPlugins(String plugin_name) {
		this.plugin_name = plugin_name;
	}
	
}
