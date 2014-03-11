package org.horsed.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * This publisher will send a heartbeat event every 60 seconds.
 * 
 * @author martin.knopf
 * 
 */
public class AutomaticHeartbeatPub {

  public static final Logger LOGGER = LoggerFactory.getLogger(AutomaticHeartbeatPub.class);

  private Context ctx;
  private Socket pub;
  private String addr;
  private String name;

  public AutomaticHeartbeatPub(String addr, String name) {
    ctx = ZMQ.context(1);
    this.addr = addr;
    this.name = name;
    initSocket(addr);
  }

  public AutomaticHeartbeatPub(Context ctx, String addr, String name) {
    this.ctx = ctx;
    this.addr = addr;
    this.name = name;
    initSocket(addr);
  }

  private void initSocket(String addr) {
    LOGGER.info("binding heartbeat publisher to " + addr);
    pub = ctx.socket(ZMQ.PUB);
    pub.connect(addr);
  }

  public void start() {
    new Thread() {

      @Override
      public void run() {
        while (!Thread.currentThread().isInterrupted()) {
          LOGGER.info("sending heartbeat event for " + name + " to " + addr);
          pub.sendMore("heartbeat");
          pub.send(name + " is alive");
          try {
            Thread.sleep(60000);
          } catch (InterruptedException e) {
          }
        }
        pub.sendMore("heartbeat");
        pub.send(name + " is shutting down");
      }

    }.start();
  }

}
