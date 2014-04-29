jeromq-toolkit [![Build Status](https://secure.travis-ci.org/Horsed/jeromq-toolkit.png)](http://travis-ci.org/Horsed/jeromq-toolkit)
==============

Helpers for simplyfied use of jeromq (zeromq for Java)

## Maven

```xml
<dependency>
  <groupId>org.horsed</groupId>
  <artifactId>jeromq-toolkit</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## API

> Publisher

```java
Pub pub = new Pub("tcp://127.0.0.1:3000");
pub.send("event", "some event data (e.g. json)");
```

> Asynchronous subscriber

```java
Sub sub = new AsyncSub("tcp://127.0.0.1:3000");   // subscriber that runs in its own thread

sub.on("event", new Handler() {                   // add inline event handler
  @Override public boolean handle(String data) {
    // handle event
  }
});

sub.connect();                                    // always invoke connect :-)
```

> Synchronous subscriber

```java
Sub sub = new SyncSub("tcp://127.0.0.1:3000");    // runs in the thread it is created in and thus blocks this thread
```

> Annotation based event handler

```java
public class MyHandler {
  
  @Subscribe("my-event")
  public void doStuff(String data) {
    // do stuff with event data
  }
  
  @Subscribe(events = {"my-event", "my-other-event"})
  public void doOtherStuff(String data) {
    // do other stuff with event data
  }
}

sub.addHandler(new MyHandler());
```

> Router socket

```java
Router router = new Router("tcp://127.0.0.1:4000") {  // asynchronously handles incoming requests
  @Override public String handle(String request) {
    // handle request
    return "response data (e.g. json)";
  }
};
```

> Request socket

```java
Req req = new Req("tcp://127.0.0.1:4000");         // connect to REP or ROUTER socket

req.onResponse(new ResponseHandler() {
  @Override public void handle(String response) {
    // handle response
  }
});

req.sendRequest("some request data (e.g. json)");  // synchronously waits for the response
```

```Req``` implements the [Lazy-Pirate-Pattern](http://zguide.zeromq.org/page:all#Client-Side-Reliability-Lazy-Pirate-Pattern) for reliable request/reply messaging: It waits 2500 ms for a response and does 3 retries. After that the request will be aborted with an exception. ```Req``` generates an identifier and sends it to the ```REP/ROUTER``` socket.
