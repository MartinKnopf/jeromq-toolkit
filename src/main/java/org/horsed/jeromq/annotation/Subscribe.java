package org.horsed.jeromq.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to annotate methods as event handlers for use with a {@link Sub}.
 * 
 * <pre>
 * 
 * public class MyHandler {
 * 
 *   &#064;Subscribe(&quot;my-event&quot;)
 *   public void doStuff(String data) {
 *     // do stuff with event data
 *   }
 * 
 *   &#064;Subscribe(events = {&quot;my-event&quot;, &quot;my-other-event&quot;})
 *   public void doOtherStuff(String data) {
 *     // do other stuff with event data
 *   }
 * }
 * 
 * new AsyncSub(&quot;tcp://127.0.0.1:3000&quot;).addHandler(new MyHandler());
 * </pre>
 * 
 * @author martin.knopf
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

  /**
   * A single event that this method should be subscribed to.
   * 
   * @return
   */
  public String value() default "";

  /**
   * An array of events that this method should be subscribed to.
   * 
   * @return
   */
  public String[] events() default {};

}
