package com.scala.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;

/*
 * tutorial from: https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html
 */

/**
 * This is the UDP sender class which instantiates a UDP sender with a given
 * port. This is to be used from the communication controller as the
 * feedback channel, all programs with a UDP listener can receive the packet and
 * display the feedback accordingly if they are on the same device, since the 
 * ip address that is used for communication is the loopback address.
 * OpenSesame displays the visual feedback dynamically after receiving the decision
 * from CLAPP.
 * 
 * @author sarah
 *
 */
public class UDPSender {

	/**
	 * The port on which the data is sent
	 */
	final int port;

	/**
	 * The data to be sent.
	 */
	private byte[] sendData;

	/**
	 * The socket to be opened. The socket is opened using the port.
	 */
	private Socket socket;

	private InetAddress host;


	/**
	 * Constructor to create a new UDP Sender. The UDP Sender will bind to a port specified in the
	 * preferences. Note that in the special case of running this APP together with
	 * OpenSesame on one phone, OS and CLAPP will try to bind to the same socket with the
	 * same IP address (localhost) which will lead to a socket exception!
	 * This is why in this constructor, the binding to the socket is done only after
	 * setReuseAddress(true) was called and therefor allows the binding of several clients to one socket.
	 * 
	 * @param port
	 * 		The port to send out packets, specified in the preferences.
	 * @param prefs
	 * 		The preferences object containing the information of the port and the ip address.
	 * @throws SocketException
	 * 		Whenever there is a problem with the creation of or the binding to the port.
	 * @throws UnknownHostException
	 * 		Whenever there is a problem with the assigned address.
	 * 
	 */
	public UDPSender(int port) throws SocketException, UnknownHostException {
		sendData = new byte[512];
		this.port = port;
		socket = new Socket(); 
		//socket.setReuseAddress(true);		
		
		// use Loopback address for local communication on the device!
		host = InetAddress.getLocalHost();
		//socket.bind(new InetSocketAddress(iP, port)); // bind explicitly after setting reuseAddr(true)
		try {
			socket.connect(new InetSocketAddress(host, port));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * Send out the packet containing the decision of the classifier.
	 * 
	 * @param msg 
	 * 		The message to be send out.
	 */
	public void sendStringMessageUDP(String msg) {
		String sentence = msg;
		sendData = sentence.getBytes();
		try {
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, host, port);
			if(socket.isConnected()){
				//socket.send(sendPacket);
				byte[] datagram_size = new byte[Integer.SIZE];
				ByteBuffer.wrap(datagram_size).order(ByteOrder.BIG_ENDIAN).asIntBuffer().put(sendData.length);
				socket.getOutputStream().write(datagram_size);
				socket.getOutputStream().write(sendData);
			}
			
			System.out.println("we sent out a packet. Content was: " + new String(sendPacket.getData()));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Method which finds the local IP address of the device and returns the first
	 * address which is NOT loopback and begins with 192.
	 * 
	 * @return 
	 * 		A valid InetAddress
	 * 
	 * @throws SocketException
	 */
	private InetAddress findLocalIPAddress() throws SocketException {
		InetAddress ia = null;
		Enumeration en = NetworkInterface.getNetworkInterfaces();
		while(en.hasMoreElements()){
		    NetworkInterface ni=(NetworkInterface) en.nextElement();
		    Enumeration ee = ni.getInetAddresses();
		    while(ee.hasMoreElements()) {
		        ia = (InetAddress) ee.nextElement();
		        // as soon as we have a reasonable address which is NOT
		        // 127.0.0.1 but begins with 192
		        if(ia.isSiteLocalAddress() && !ia.isLoopbackAddress()) {
		        	return ia;
		        }
		    }
		 }
		return ia;
	}

}
