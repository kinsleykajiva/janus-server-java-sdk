package io.github.kinsleykajiva.janus.admin;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JanusAdminMonitor {
    private static final Logger logger = LoggerFactory.getLogger(JanusAdminMonitor.class);
    private final List<JanusAdminEventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(JanusAdminEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(JanusAdminEventListener listener) {
        listeners.remove(listener);
    }

    public void dispatchEvent(JSONObject event) {
        logger.debug("Dispatching admin event: {}", event.toString());
        for (JanusAdminEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                logger.error("Error in JanusAdminEventListener while processing event: {}", event.toString(), e);
            }
        }
    }
}
