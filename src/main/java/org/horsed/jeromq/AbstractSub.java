package org.horsed.jeromq;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import org.horsed.jeromq.annotation.Subscribe;

/**
 * 
 * @author martin.knopf
 * 
 */
public abstract class AbstractSub implements Sub {

  public static final Logger LOGGER = LoggerFactory.getLogger(AbstractSub.class);

  protected final String pub;
  private final Map<String, List<Handler>> handlers;
  private Handler anytimeHandler;
  private final Context ctx;

  private boolean connected = false;

  public AbstractSub(final String pub) {
    this(ZMQ.context(1), pub);
  }

  public AbstractSub(final Context ctx, final String pub) {
    this.ctx = ctx;
    this.pub = pub;
    handlers = new HashMap<String, List<Handler>>();
  }

  @Override
  public Sub on(String event, Handler handler) {
    if (connected) {
      throw new RuntimeException("AbstractSub is already connected");
    }

    LOGGER.info("subscribing to " + event + " from " + pub);
    if (handlers.containsKey(event)) {
      handlers.get(event).add(handler);
    } else {
      List<Handler> handlerz = new ArrayList<Handler>();
      handlerz.add(handler);
      handlers.put(event, handlerz);
    }
    return this;
  }

  @Override
  public Sub onAny(Handler handler) {
    this.anytimeHandler = handler;
    return this;
  }

  @Override
  public abstract void connect();

  protected void startEventLoop() {
    Socket sub = connectSubSocket();
    connected = true;

    while (!Thread.currentThread().isInterrupted()) {
      try {
        String e = sub.recvStr();
        String data = sub.recvStr();
        handle(e, data);

        if (anytimeHandler == null && handlers.isEmpty()) {
          break;
        }
      } catch (Exception e) {
        LOGGER.warn("Error handling event. Will close sub socket.", e);

        if (e instanceof ZMQException) {
          LOGGER.error("Will close sub socket.");
          break;
        }
      }
    }
    LOGGER.info("closing subscriber");
    sub.close();
  }

  Socket connectSubSocket() {
    Socket sub = ctx.socket(ZMQ.SUB);

    if (handlers.isEmpty()) {
      sub.subscribe("".getBytes());
    } else {
      for (String event : handlers.keySet()) {
        sub.subscribe(event.getBytes());
      }
    }
    sub.connect(pub);
    return sub;
  }

  void handle(String e, String data) {
    if (handlers.containsKey(e)) {
      LOGGER.info("handling event " + e + " with handler " + handlers.get(e));
      for (Handler handler : handlers.get(e)) {
        try {
          if (handler.handle(data)) {
            LOGGER.info("removing handler for event " + e);
            handlers.remove(e);
          }
        } catch (Exception e1) {
          LOGGER.error("Error handling event " + e + " with handler " + handler);
        }
      }
    }

    if (anytimeHandler != null) {
      LOGGER.info("handling event " + e + " with handler " + handlers.get(e));
      anytimeHandler.handle(data);
    }
  }

  @Override
  public Sub addHandler(final Object handler) {
    Method[] methods = handler.getClass().getMethods();

    for (final Method method : methods) {

      Subscribe subscriber = method.getAnnotation(Subscribe.class);

      if (subscriber != null) {

        for (final String event : events(subscriber)) {
          this.on(event, new Handler() {

            @Override
            public boolean handle(String data) {
              try {
                method.invoke(handler, data);
              } catch (Exception e) {
                LOGGER.error("Error handling event " + event + " with method " + method);
              }
              return false;
            }
          });
        }
      }
    }

    return this;
  }

  List<String> events(Subscribe subscriber) {
    final String event2 = subscriber.value();
    String[] eventz = subscriber.events();

    List<String> events = new ArrayList<String>();
    if (event2 != null && !event2.isEmpty()) {
      events.add(event2);
    }
    for (String e : eventz) {
      if (e != null && !e.isEmpty()) {
        events.add(e);
      }
    }
    return events;
  }

}
