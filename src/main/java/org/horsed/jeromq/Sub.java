package org.horsed.jeromq;

import org.zeromq.ZMQ.Socket;

import org.horsed.jeromq.annotation.Subscribe;

/**
 * Wrapper of a {@code ZMQ.SUB} {@link Socket}.
 * 
 * @author martin.knopf
 * 
 */
public interface Sub {

  /**
   * Add a {@link Handler} for the given event. {@link Handler#handle(String)} will be called <strong>whenever</strong>
   * the event is received.
   * 
   * @param event
   * @param handler
   * @return this instance for chaining
   */
  public abstract Sub on(String event, Handler handler);

  /**
   * Add a {@link Handler} for any event. {@link Handler#handle(String)} will be called <strong>whenever</strong>
   * an event is received.
   * 
   * @param handler
   * @return
   */
  public abstract Sub onAny(Handler handler);

  /**
   * Adds a {@link Handler} for any method of the given Object annotated with {@link Subscribe}, which will invoke the
   * method on the specified event(s).
   * 
   * @param handler
   * @return
   */
  public abstract Sub addHandler(final Object handler);

  /**
   * Starts the event loop of this subscriber.
   */
  public void connect();

}
