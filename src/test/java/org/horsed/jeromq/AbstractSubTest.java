package org.horsed.jeromq;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zeromq.ZMQ.Context;

import org.horsed.jeromq.annotation.Subscribe;

public class AbstractSubTest {

  private AbstractSub sub;

  @Before
  public void setUp() {
    this.sub = new AbstractSub(Mockito.mock(Context.class), "tcp://127.0.0.1:3000") {

      @Override
      public void connect() {
      }

    };
  }

  @Test
  public void shouldInvokeHandler() {
    Handler mockHandler = Mockito.mock(Handler.class);
    sub.on("abc", mockHandler);

    sub.handle("abc", "data");

    Mockito.verify(mockHandler).handle("data");
  }

  @Test
  public void shouldInvokeHandlerMultipleTimes() {
    Handler mockHandler = Mockito.mock(Handler.class);
    sub.on("abc", mockHandler);

    sub.handle("abc", "data");
    sub.handle("abc", "data");

    Mockito.verify(mockHandler, Mockito.times(2)).handle("data");
  }

  @Test
  public void shouldInvokeHandlerAgainAfterError() {
    final StringBuilder sb = new StringBuilder();
    sub.on("abc", new Handler() {

      @Override
      public boolean handle(String data) {
        sb.append(data);
        throw new RuntimeException();
      }

    });

    sub.handle("abc", "data");
    sub.handle("abc", "data");

    assertEquals("datadata", sb.toString());
  }

  @Test
  public void shouldInvokeMultipleHandlers() {
    Handler mockHandler1 = Mockito.mock(Handler.class);
    Handler mockHandler2 = Mockito.mock(Handler.class);
    sub.on("abc", mockHandler1);
    sub.on("abc", mockHandler2);

    sub.handle("abc", "data");

    Mockito.verify(mockHandler1).handle("data");
    Mockito.verify(mockHandler2).handle("data");
  }

  @Test
  public void shouldNotInvokeHandlerMethodOnUnspecifiedEvent() {
    Handler mockHandler = Mockito.mock(Handler.class);
    sub.on("abc", mockHandler);

    sub.handle("def", "data");

    Mockito.verify(mockHandler, Mockito.never()).handle("data");
  }

  @Test
  public void shouldInvokeHandlerMethodOnAnyEvent() {
    Handler mockHandler = Mockito.mock(Handler.class);
    sub.onAny(mockHandler);

    sub.handle("abc", "data");

    Mockito.verify(mockHandler).handle("data");
  }

  @Test
  public void shouldRemoveHandlerAfterInvokingIt() {
    Handler mockHandler = Mockito.mock(Handler.class);
    Mockito.when(mockHandler.handle("data")).thenReturn(true);
    sub.on("abc", mockHandler);

    sub.handle("abc", "data");
    sub.handle("abc", "data");

    Mockito.verify(mockHandler, Mockito.times(1)).handle("data");
  }

  @Test(expected = RuntimeException.class)
  public void shouldNotAcceptHandlerAfterSocketStarted() {
    sub.startEventLoop();

    sub.on("abc", Mockito.mock(Handler.class));
  }

  @Test
  public void shouldInvokeAnnotatedHandlerMethod() {
    final StringBuilder sb = new StringBuilder();
    sub.addHandler(new Handler() {

      @Subscribe("ghi")
      @Override
      public boolean handle(String data) {
        sb.append(data);
        return false;
      }
    });

    sub.handle("ghi", "data");

    assertEquals("data", sb.toString());
  }

  @Test
  public void shouldInvokeAnnotatedHandlerMethodMultipleTimes() {
    final StringBuilder sb = new StringBuilder();
    sub.addHandler(new Handler() {

      @Subscribe("ghi")
      @Override
      public boolean handle(String data) {
        sb.append(data);
        return false;
      }
    });

    sub.handle("ghi", "data");
    sub.handle("ghi", "data");

    assertEquals("datadata", sb.toString());
  }

  @Test
  public void shouldInvokeAnnotatedHandlerMethodAgainAfterError() {
    final StringBuilder sb = new StringBuilder();
    sub.addHandler(new Handler() {

      @Subscribe("ghi")
      @Override
      public boolean handle(String data) {
        sb.append(data);
        throw new RuntimeException();
      }
    });

    sub.handle("ghi", "data");
    sub.handle("ghi", "data");

    assertEquals("datadata", sb.toString());
  }

  @Test
  public void shouldInvokeMultipleAnnotatedHandlerMethods() {
    final StringBuilder sb = new StringBuilder();
    sub.addHandler(new Handler() {

      @Subscribe("ghi")
      @Override
      public boolean handle(String e) {
        sb.append(e);
        return false;
      }

      @Subscribe("ghi")
      public void handle2(String o) {
        sb.append(o);
      }

    });

    sub.handle("ghi", "data");

    assertEquals("datadata", sb.toString());
  }

  @Test
  public void shouldNotInvokeAnnotatedHandlerMethodOnUnspecifiedEvent() {
    final StringBuilder sb = new StringBuilder();
    sub.addHandler(new Handler() {

      @Subscribe("ghi")
      @Override
      public boolean handle(String data) {
        sb.append(data);
        return false;
      }
    });

    sub.handle("abc", "data");

    assertEquals("", sb.toString());
  }

  @Test
  public void shouldInvokeAnnotatedHandlerMethodOnEverySpecifiedEvent() {
    final StringBuilder sb = new StringBuilder();
    sub.addHandler(new Handler() {

      @Subscribe("abc")
      @Override
      public boolean handle(String e) {
        sb.append(e);
        return false;
      }

      @Subscribe(events = {"def", "ghi"})
      public void handle2(String o) {
        sb.append(o);
      }

    });

    sub.handle("abc", "data");
    sub.handle("def", "data");

    assertEquals("datadata", sb.toString());
  }

}
