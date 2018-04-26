package com.scala.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.scala.tools.SampleBuffer;

import android.support.annotation.NonNull;

/**
 * This class is used to send out the generated templates via TCP. The protocol specifications
 * were taken from the EEG2Go Presenter APP which demands a certain structure in the UDP
 * package.
 * 
 * @author sarah
 *
 */
public class TCPTemplateSender {

	public void sendOutTemplates(SampleBuffer templateBuffer) {

		int sR = 250;
		int lengthOfDataChunk = 4; // seconds
		int numSamples = sR*lengthOfDataChunk;
		int numClasses = 2;
		int channelsPerClass = 1;
		
		float[] leftTemplate = templateBuffer.getValuesFromOneChannelAsFloat(0);
		float[] rightTemplate = templateBuffer.getValuesFromOneChannelAsFloat(1);
		
		
		byte[] bytesMessage = new byte[Float.SIZE*numSamples];
		FloatBuffer buffer = ByteBuffer.wrap(bytesMessage).asFloatBuffer();
		
		buffer.put((float)sR)
				.put((float)numClasses)
				.put((float) channelsPerClass)
				.put((float) leftTemplate.length)
				.put(leftTemplate)
				.put((float) channelsPerClass)
				.put((float) rightTemplate.length)
				.put(rightTemplate);
				
		SendDatagram(bytesMessage, 50008);
	}

	private synchronized int SendDatagram(@NonNull byte[] bytesMessage, int port) {
		try {
			InetAddress host = InetAddress.getLocalHost(); // receiver host
			return SendDatagram(bytesMessage, host, port);
		} catch (UnknownHostException e) {
			return -1; // error
		}
		
	}

	private int SendDatagram(byte[] bytesMessage, InetAddress host, int port) {
		Socket socket = new Socket();
		int i_datagram_bytes = -1;
		int timeout = 2000;
		try {
			socket.connect(new InetSocketAddress(host, port), timeout);
			if(socket.isConnected()){
				// claim buffer
				byte[] datagram_size = new byte[Integer.SIZE];
				ByteBuffer.wrap(datagram_size).order(ByteOrder.BIG_ENDIAN).asIntBuffer().put(bytesMessage.length);
				socket.getOutputStream().write(datagram_size);
				socket.getOutputStream().write(bytesMessage);
				i_datagram_bytes = bytesMessage.length;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (!socket.isClosed()){
				try {
					socket.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return i_datagram_bytes; // number of transmitted bytes
	}

}
