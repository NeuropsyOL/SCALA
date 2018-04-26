package com.scala.udp;

/**
 * Interface that is to be implemented by all classes that
 * are responsible for the communication via UDP.
 * This interface must be implmented whenever we want to use 
 * an incoming UDP signal as a trigger to do something with incoming
 * data via TCP.
 * 
 * @author sarah
 *
 */
public interface IncomingDataCallback {

	public void signalResult(String msg, double timestamp);

}
