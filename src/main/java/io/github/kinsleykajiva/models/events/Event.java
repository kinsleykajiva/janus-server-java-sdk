package io.github.kinsleykajiva.models.events;

/** Represents an event in the system. */
public class Event {
  private String plugin;

  /**
   * Constructs a new Event object with the specified plugin.
   *
   * @param plugin the plugin associated with the event
   */
  public Event(String plugin) {
    this.plugin = plugin;
  }

  /**
   * Gets the plugin associated with the event.
   *
   * @return the plugin associated with the event
   */
  public String getPlugin() {
    return plugin;
  }
}
