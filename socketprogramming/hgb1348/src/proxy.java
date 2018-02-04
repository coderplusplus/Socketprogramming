
import java.util.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/*
 * This class creates proxy for  the server client communication.
 */
public class proxy extends Thread {
	int port;
	int alternatePort;
	static long time = new Date().getTime();
	static String errorMessage = "";
	ServerSocket server;
	String connectionType;
	int proxyUDP;
	int proxyTCP;
	String serverAddress;
	boolean TCPpresent;

	/*
	 * This constructor inialises all the variables needed by the proxy
	 */
	public proxy(String connectionType, int proxyUDP, int proxyTCP, int port, int alternatePort, String serverAddress,
			boolean present) throws IOException {
		this.connectionType = connectionType;
		this.alternatePort = alternatePort;
		this.port = port;
		this.proxyUDP = proxyUDP;
		this.proxyTCP = proxyTCP;
		this.serverAddress = serverAddress;
		this.TCPpresent = present;
	}

	/*
	 * It is the default run method of the proxy thread. This method starts the
	 * TCP and UDP listening port of the proxy
	 */

	public void run() {
		try {
			new TCP().start();

			new UDP().start();

		} catch (Exception e) {
		}

	}

	/*
	 * This is the inner class of the proxy class. It opens the TCP connection
	 * of the server
	 */
	class TCP extends Thread {

		/*
		 * It is the default run method of the TCPproxy thread. This method
		 * starts the TCP and UDP listening port of the proxy
		 */
		public void run() {
			try {
				connectTCP();
			} catch (Exception e) {
			}
		}

		/*
		 * This function opens the TCP connection. It receives the message from
		 * client and forward it to the origin server.
		 */
		public void connectTCP() throws IOException, ClassNotFoundException {

			ServerSocket server = new ServerSocket(alternatePort);
			System.out.println("Proxy: Listening on TCP: " + alternatePort);
			messageObject infoFromServer;
			byte[] fromServerDatatoProxy = new byte[1024];
			while (true) {

				Socket sock = server.accept();

				System.out.println("Client connected via TCP!");

				InputStream inFromClient = sock.getInputStream();

				ObjectInputStream objectInFromClient = new ObjectInputStream(inFromClient);

				messageObject infoFromClient = (messageObject) objectInFromClient.readObject();

				infoFromClient.noOfHops++;
				infoFromClient.IPAddresses.add(InetAddress.getLocalHost().toString());
				infoFromClient.timings.add(System.currentTimeMillis());

				if (connectionType.equals("-t") || infoFromClient.clientConnectionType.equals("-t")) {
					Socket proxyToServer = new Socket(serverAddress, proxyTCP);
					OutputStream outToServer = proxyToServer.getOutputStream();
					ObjectOutputStream objectOutputToServer = new ObjectOutputStream(outToServer);

					objectOutputToServer.writeObject(infoFromClient);

					InputStream inFromServer = proxyToServer.getInputStream();
					ObjectInputStream objectInputFromServer = new ObjectInputStream(inFromServer);

					infoFromServer = (messageObject) objectInputFromServer.readObject();

				} else {
					DatagramSocket proxyToServer = new DatagramSocket();

					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(infoFromClient);
					byte[] data = outputStream.toByteArray();
					DatagramPacket proxyToServerPacket = new DatagramPacket(data, data.length,
							InetAddress.getByName(serverAddress), proxyUDP);

					proxyToServer.send(proxyToServerPacket);

					// reading from server
					byte[] incomingDatafromServer = new byte[1024];
					DatagramPacket incomingPacketFromServer = new DatagramPacket(incomingDatafromServer,
							incomingDatafromServer.length);
					proxyToServer.receive(incomingPacketFromServer);
					fromServerDatatoProxy = incomingPacketFromServer.getData();

					ByteArrayInputStream inFromServer = new ByteArrayInputStream(fromServerDatatoProxy);
					ObjectInputStream objectFromServer = new ObjectInputStream(inFromServer);

					infoFromServer = (messageObject) objectFromServer.readObject();
				}
				OutputStream out = sock.getOutputStream();
				ObjectOutputStream oout = new ObjectOutputStream(out);
				oout.writeObject(infoFromServer);

			}

		}
	}

	/*
	 * This is the inner class of the proxy class. It opens the UDP connection
	 * of the server
	 */
	class UDP extends Thread {

		/*
		 * It is the default run method of the TCPproxy thread. This method
		 * starts the TCP and UDP listening port of the proxy
		 */
		public void run() {
			try {
				connectUDP();
			} catch (Exception e) {
			}
		}

		/*
		 * This function opens the TCP connection. It receives the message from
		 * client and forward it to the origin server.
		 */
		public void connectUDP() throws IOException, ClassNotFoundException {
			messageObject infoFromServer;
			byte[] dataToClient;
			DatagramSocket serverSocket = new DatagramSocket(port);
			System.out.println("Proxy: Listening on UDP: " + port);
			byte[] fromServerDatatoProxy = new byte[1024];
			while (true) {
				try {

					byte[] incomingData = new byte[1024];
					DatagramPacket incomingPacketFromClient = new DatagramPacket(incomingData, incomingData.length);
					serverSocket.receive(incomingPacketFromClient);
					System.out.println("Client connected via UDP");

					InetAddress IPAddress = incomingPacketFromClient.getAddress();
					byte[] fromServerData = incomingPacketFromClient.getData();
					ByteArrayInputStream in = new ByteArrayInputStream(fromServerData);
					ObjectInputStream ois = new ObjectInputStream(in);

					messageObject infoFromClient = (messageObject) ois.readObject();
					infoFromClient.noOfHops++;
					infoFromClient.IPAddresses.add(InetAddress.getLocalHost().toString());
					infoFromClient.timings.add(System.currentTimeMillis());

					// check if the connection to server is tcp
					if (connectionType.equals("-t") || infoFromClient.clientConnectionType.equals("-t")) {
						Socket proxyToServer = new Socket(serverAddress, proxyTCP);
						OutputStream outToServer = proxyToServer.getOutputStream();
						ObjectOutputStream objectOutputToServer = new ObjectOutputStream(outToServer);

						objectOutputToServer.writeObject(infoFromClient);

						InputStream inFromServer = proxyToServer.getInputStream();
						ObjectInputStream objectInputFromServer = new ObjectInputStream(inFromServer);

						infoFromServer = (messageObject) objectInputFromServer.readObject();

					}
					// if the connection to server is udp
					else {
						DatagramSocket proxyToServer = new DatagramSocket();

						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						ObjectOutputStream os = new ObjectOutputStream(outputStream);
						os.writeObject(infoFromClient);
						byte[] data = outputStream.toByteArray();
						DatagramPacket proxyToServerPacket = new DatagramPacket(data, data.length,
								InetAddress.getByName(serverAddress), proxyUDP);

						proxyToServer.send(proxyToServerPacket);

						// reading from server
						byte[] incomingDatafromServer = new byte[1024];
						DatagramPacket incomingPacketFromServer = new DatagramPacket(incomingDatafromServer,
								incomingDatafromServer.length);
						proxyToServer.receive(incomingPacketFromServer);
						fromServerDatatoProxy = incomingPacketFromServer.getData();

						ByteArrayInputStream inFromServer = new ByteArrayInputStream(fromServerDatatoProxy);
						ObjectInputStream objectFromServer = new ObjectInputStream(inFromServer);

						infoFromServer = (messageObject) objectFromServer.readObject();

					}

					ByteArrayOutputStream outputProxyToClient = new ByteArrayOutputStream();
					ObjectOutputStream objectFromProxyToServer = new ObjectOutputStream(outputProxyToClient);
					objectFromProxyToServer.writeObject(infoFromServer);
					dataToClient = outputProxyToClient.toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(dataToClient, dataToClient.length,
							incomingPacketFromClient.getAddress(), incomingPacketFromClient.getPort());
					serverSocket.send(sendPacket);

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
