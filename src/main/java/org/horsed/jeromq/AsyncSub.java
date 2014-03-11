package org.horsed.jeromq;

import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * Asynchronous {@code ZMQ.SUB} {@link Socket}.
 * 
 * @author martin.knopf
 * 
 */
public class AsyncSub extends AbstractSub implements Sub {

  public AsyncSub(String pub) {
    super(pub);
  }

  public AsyncSub(Context ctx, String pub) {
    super(ctx, pub);
  }

  @Override
  public void connect() {
    new Thread() {

      @Override
      public void run() {
        startEventLoop();
      }

    }.start();
  }

}
