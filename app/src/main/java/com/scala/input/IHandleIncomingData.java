package com.scala.input;

import java.io.IOException;

/**
 * Interface for the receiving classes of incoming data. Incoming data
 * streams/signals should be resolved and data should be stored into buffers.
 * 
 * @author sarah
 *
 */
public interface IHandleIncomingData {

	public boolean resolveIncomingStream() throws Exception;

	public void putDataInBuffer(double timestamp);

}
