package org.horsed.jeromq;

/**
 * {@code ZMQ.REQ} socket wrapper.
 * 
 * @author martin.knopf
 * 
 */
public interface IReq {

  public abstract IReq onResponse(ResponseHandler handler);

  public abstract void sendRequest(String request) throws Exception;

}
