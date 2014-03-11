package org.horsed.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZThread.IAttachedRunnable;

/**
 * Wrapper of a {@code ZMQ.PUB} {@link Socket}.
 * 
 * @author martin.knopf
 * 
 */
public class Pub implements IAttachedRunnable {

  public static final Logger LOGGER = LoggerFactory.getLogger(Pub.class);

  private Context ctx;
  private Socket pub;
  private String addr;

  public Pub(String addr) {
    ctx = ZMQ.context(1);
    this.addr = addr;
    initSocket(addr);
  }

  public Pub(Context ctx, String addr) {
    this.ctx = ctx;
    this.addr = addr;
    initSocket(addr);
  }

  private void initSocket(String addr) {
    LOGGER.info("binding publisher to " + addr);
    pub = ctx.socket(ZMQ.PUB);
    pub.connect(addr);
  }

  public boolean send(String event, byte[] payload) {
    LOGGER.info("sending event " + event + " from addr " + addr);
    pub.sendMore(event);
    return pub.send(payload);
  }

  public boolean send(String event, String payload) {
    return this.send(event, payload.getBytes());
  }

  @Override
  public void run(Object[] args, ZContext ctx, Socket pipe) {
    pub = ctx.createSocket(ZMQ.PUB);
    pub.bind(addr);
  }

}
