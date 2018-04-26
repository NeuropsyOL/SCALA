package com.scala.udp;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Objects;

/*
 * from: https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html
 */

/**
 * This is the receiving class for UDP packets. It is working
 * together with the CommunicationController class to signal the storage of EEG data into the
 * sampleBuffer if the correct UDP marker gets in.
 * 
 * UDP packets are expected to be UTF-8 encoded strings. If the incoming message
 * is 'trigger', the incoming EEG data should be recorded into the sampleBuffer.
 * 
 * This class, as well as the LSLMarkerReceiver class, is notifying the EEGDataReceiver
 * via the IncomingDataCallback which has to be passed in the constructor.
 * This notification will lead to the storage of EEG samples in a buffer which
 * is then passed to the MainController for further signal analysis.
 * 
 * @author sarah
 *
 */
public class UDPListener implements Runnable {

	private static final int BUFFER_SIZE = 1024; // bytes
	DatagramSocket socket;
	private boolean listening = true;
	private int howManyPacketsReceived = 0;
	private final IncomingDataCallback callback;

	/**
	 * Create a UDPListener instance with an object of the Callback to notify 
	 * the CommunicationController whenever we received a UDP package.
	 * 
	 * @param port The port on which we listen for packagess
	 * @param cb The object of the Callback to notify the CommunicationController
	 * @throws SocketException Whenever we cannot create a DatagramSocket with the given port.
	 */
	public UDPListener(int port, IncomingDataCallback cb) throws SocketException {
		socket = new DatagramSocket(port);
		socket.setSoTimeout(0);
		socket.setReuseAddress(true);
		this.callback = Objects.requireNonNull(cb);

		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * Listen to UDP packages on the specified port and notify the CommunicationController
	 * whenever the "correct"  message was detected. We listen until CLAP is being terminated.
	 */
	@Override
	public void run() {
		byte[] udpData = new byte[BUFFER_SIZE];
		try {
			// listen only once, until one packet came in
			while (listening) {
				DatagramPacket receivePacket = new DatagramPacket(udpData, udpData.length);
				socket.receive(receivePacket);
				// TODO add a timestamp which is compatible to the LSL stamps to the package
				double timestamp = 0.0;
				howManyPacketsReceived++;

				System.out.println( "number of packets received: " + howManyPacketsReceived);
				String decodedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength(), UTF_8);

				/*
				 * callback to the input controller which needs to know, when to
				 * store data in the buffer
				 */
				callback.signalResult(decodedMessage, timestamp);

			}
		} catch (IOException e) {
			e.printStackTrace();
			listening = false;
		} finally {
			socket.close();
			listening = false;

		}
	}
}
