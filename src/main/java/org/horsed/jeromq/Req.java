package org.horsed.jeromq;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

/**
 * {@link IReq} implementing the <a
 * href="http://zguide.zeromq.org/page:all#Client-Side-Reliability-Lazy-Pirate-Pattern">Lazy Pirate Pattern</a> for
 * retrying requests and finally closing the {@link Socket} when the server doesn't respond.
 * 
 * @author martin.knopf
 * 
 */
public class Req implements IReq {

  private static final Logger LOGGER = LoggerFactory.getLogger(Req.class);
  private static Random rand = new Random(System.nanoTime());

  private final static int REQUEST_TIMEOUT = 2500;
  private final static int REQUEST_RETRIES = 3;

  private String responder;
  private Context ctx;
  private Socket requester;
  private ResponseHandler handler;

  public Req(String responder) {
    this.responder = responder;

    ctx = ZMQ.context(1);
    LOGGER.info("Connecting request socket to " + responder);
    connect();
  }

  private void connect() {
    requester = ctx.socket(ZMQ.REQ);
    requester.setIdentity(String.format("%04X-%04X", rand.nextInt(), rand.nextInt()).getBytes());
    requester.connect(responder);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.horsed.jeromq.IReq#onResponse(org.horsed.jeromq.ResponseHandler)
   */
  @Override
  public IReq onResponse(ResponseHandler handler) {
    this.handler = handler;
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.horsed.jeromq.IReq#sendRequest(java.lang.String)
   */
  @Override
  public void sendRequest(String request) throws Exception {
    int retriesLeft = REQUEST_RETRIES;

    while (retriesLeft > 0 && !Thread.currentThread().isInterrupted()) {
      LOGGER.info("Sending request " + request);
      requester.send(request);

      int expect_reply = 1;
      while (expect_reply > 0) {
        PollItem items[] = {new PollItem(requester, Poller.POLLIN)};
        int rc = ZMQ.poll(items, REQUEST_TIMEOUT);
        if (rc == -1) {
          break; //  Interrupted
        }

        if (items[0].isReadable()) {

          try {
            String response = requester.recvStr();

            retriesLeft = 0;
            expect_reply = 0;

            LOGGER.info("Handling response");
            this.handler.handle(response);
          } catch (Exception e) {
            LOGGER.warn("Error during request/response", e);

            if (e instanceof ZMQException) {
              LOGGER.error("Will close request socket");
              requester.close();
              ctx.close();
              LOGGER.info("Closed request socket, which was connected to " + responder);
              throw new Exception("Error during request/response", e);
            }
          }

        } else if (--retriesLeft == 0) {
          LOGGER.error("Service " + responder + " seems to be unavailable");
          throw new Exception("Service " + responder + " seems to be unavailable");
        } else {
          LOGGER.warn("No response from " + responder);
          requester.close();
          LOGGER.info("Reconnecting request socket to " + responder);
          connect();
        }
      }
    }
  }
}
