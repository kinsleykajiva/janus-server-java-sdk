package io.github.kinsleykajiva.janus.admin.messages;

import org.json.JSONObject;

public class StartPcap {
    private final String transaction;
    private final long sessionId;
    private final long handleId;
    private final String folder;
    private final String filename;

    public StartPcap(String transaction, long sessionId, long handleId, String folder, String filename) {
        this.transaction = transaction;
        this.sessionId = sessionId;
        this.handleId = handleId;
        this.folder = folder;
        this.filename = filename;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("janus", "start_pcap");
        json.put("transaction", transaction);
        json.put("session_id", sessionId);
        json.put("handle_id", handleId);
        if (folder != null && !folder.isEmpty()) {
            json.put("folder", folder);
        }
        if (filename != null && !filename.isEmpty()) {
            json.put("filename", filename);
        }
        return json;
    }
}
