package org.horsed.jeromq;

public interface Handler {

  /**
   * Callback for an event.
   * 
   * @param data
   * @return {@code true}, if this {@link Handler} should be removed after being called the first time.
   */
  public boolean handle(String data);

}
