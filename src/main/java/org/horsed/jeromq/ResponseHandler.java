package org.horsed.jeromq;

/**
 * Handler for a {@code REQ/REP} response.
 * 
 * @author martin.knopf
 * 
 */
public interface ResponseHandler {

  /**
   * Callback for a REQ/REP response.
   * 
   * @param response
   */
  public void handle(String response);

}
