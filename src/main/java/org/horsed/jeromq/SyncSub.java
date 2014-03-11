package org.horsed.jeromq;

import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * Synchronous {@code ZMQ.SUB} {@link Socket}.
 * 
 * @author martin.knopf
 * 
 */
public class SyncSub extends AbstractSub {

  public SyncSub(String pub) {
    super(pub);
  }

  public SyncSub(Context ctx, String pub) {
    super(ctx, pub);
  }

  @Override
  public void connect() {
    startEventLoop();
  }

}
