
import java.util.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/*
 * This class creates a server and connects it with the client or proxy 
 */
public class server extends Thread {
	int setTime;
	String username;
	String password;
	int port;
	int alternatePort;
	static long time;
	static String errorMessage = "";

	/*
	 * This constructor inialises all the variables needed by the server
	 */
	public server(int setTime, String username, String password, int port, int altport) {
		this.setTime = setTime;
		this.username = username;
		this.password = password;
		this.port = port;
		this.alternatePort = altport;
	}

	/*
	 * It is the default run method of the server thread. This method starts the
	 * TCP and UDP listening port of the server
	 */
	public void run() {
		try {
			new TCP().start();

			new UDP().start();

		} catch (Exception e) {
		}

	}

	/*
	 * This is the inner class of the server class. It opens the TCP connection
	 * of the server
	 */
	class TCP extends Thread {

		public void run() {
			try {
				connectTCP();
			} catch (Exception e) {
			}
		}

		/*
		 * This method opens the TCP connection. Receeives the information from
		 * the client, modify the message and send it back to the client
		 */
		public void connectTCP() throws IOException {
			ServerSocket server = new ServerSocket(alternatePort);
			Socket sock;
			while (true) {

				System.out.println("Server: Listening on TCP: " + alternatePort);
				sock = server.accept();

				System.out.println("Client/Proxy connected via TCP!");

				InputStream in = sock.getInputStream();
				ObjectInputStream oin = new ObjectInputStream(in);

				OutputStream out = sock.getOutputStream();
				ObjectOutputStream oout = new ObjectOutputStream(out);
				try {

					messageObject infoFromClient = (messageObject) oin.readObject();
					infoFromClient.noOfHops++;
					infoFromClient.IPAddresses.add(InetAddress.getLocalHost().toString());
					infoFromClient.timings.add(System.currentTimeMillis());

					if (infoFromClient != null) {

						// check 1: client would set time or default
						clientSetTime(infoFromClient);

						infoFromClient.time = setTime;
						infoFromClient.error = errorMessage;
						oout.writeObject(infoFromClient);

					}
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}

			}
		}
	}

	/*
	 * This is the inner class of the server class. It opens the UDP connection
	 * of the server
	 */
	class UDP extends Thread {

		public void run() {
			try {
				connectUDP();
			} catch (Exception e) {
			}
		}

		/*
		 * This method opens the TCP connection. Receives the information from
		 * the client, modify the message and send it back to the client
		 */
		public void connectUDP() throws IOException {

			DatagramSocket serverSocket = new DatagramSocket(port);
			while (true) {
				try {
					System.out.println("Server: Listening on UDP: " + port);

					byte[] incomingData = new byte[1024];
					DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
					serverSocket.receive(incomingPacket);
					System.out.println("Client/Proxy connected via UDP");
					InetAddress IPAddress = incomingPacket.getAddress();
					byte[] fromServerData = incomingPacket.getData();
					ByteArrayInputStream in = new ByteArrayInputStream(fromServerData);
					ObjectInputStream ois = new ObjectInputStream(in);

					messageObject infoFromClient = (messageObject) ois.readObject();
					infoFromClient.noOfHops++;
					infoFromClient.IPAddresses.add(InetAddress.getLocalHost().toString());
					infoFromClient.timings.add(System.currentTimeMillis());

					if (infoFromClient != null) {

						clientSetTime(infoFromClient);
						infoFromClient.time = setTime;
						infoFromClient.error = errorMessage;

						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						ObjectOutputStream os = new ObjectOutputStream(outputStream);
						os.writeObject(infoFromClient);
						byte[] data = outputStream.toByteArray();

						DatagramPacket sendPacket = new DatagramPacket(data, data.length, incomingPacket.getAddress(),
								incomingPacket.getPort());
						serverSocket.send(sendPacket);

					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

		}
	}

	// function for client to set the time
	public void clientSetTime(messageObject fromClient) {
		if (fromClient.setTime != 0) {

			// check if the username and password match
			credentials(fromClient);

		}

	}

	// function to authenticate client
	public void credentials(messageObject fromClient) {
		if (username.equals(fromClient.username) && password.equals(fromClient.password)) {
			setTime = fromClient.setTime;
			errorMessage = "";
		} else {
			errorMessage = "Sorry,credentials didn't match";
		}
	}

}