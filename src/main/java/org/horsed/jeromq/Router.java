package org.horsed.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

/**
 * Asynchronous {@code ZMQ.REP} socket wrapper.
 * 
 * @author martin.knopf
 * 
 */
public abstract class Router {

  public static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

  public Router(final String addr) {

    new Thread() {

      @Override
      public void run() {
        Context ctx = ZMQ.context(1);
        Socket router = ctx.socket(ZMQ.ROUTER);
        LOGGER.info("Binding reply socket to " + addr);
        router.bind(addr);

        while (!Thread.currentThread().isInterrupted()) {
          try {
            String id = router.recvStr();
            router.sendMore(id);
            router.recv();
            String request = router.recvStr();
            router.sendMore("");

            LOGGER.info("Handling request " + request);
            String response = handle(request);

            LOGGER.info("Sending response");
            router.send(response);
          } catch (Exception e) {
            LOGGER.warn("Error handling request", e);

            if (e instanceof ZMQException) {
              LOGGER.error("Will close reply socket");
              break;
            }
          }
        }
        router.close();
        ctx.close();
        LOGGER.info("Closed reply socket " + addr);
      }

    }.start();
  }

  public abstract String handle(String request);
}
