
/*
 * This class creates a client for the communication.
 * It decides the connection type of the client with the server or proxyS
 */
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

/*
 * This class defines the client and sets up the connection
 * with the server based on the based given 
 */
public class client extends Thread {
	String connectionType = "";
	int setTime = 0;
	String username;
	String password;
	int numberOfQueries;
	String serverAddress;
	int port;
	static InetAddress IPAddress;
	String timeUTC;
	boolean utc = false;
	int type = 1;
	static long totalRTT;
	long start;
	long end;

	/*
	 * This constructor inialises all the values needed by the client to set up
	 * a connection
	 */
	public client(String connection, int setTime, String username, String password, String UTC, int queries,
			String serveraddress, int port) {
		connectionType = connection;
		this.setTime = setTime;
		this.username = username;
		this.password = password;
		this.timeUTC = UTC;
		this.numberOfQueries = queries;
		this.serverAddress = serveraddress;
		this.port = port;

		if (timeUTC.equals("-z"))
			utc = true;
		
	}

	/*
	 * This function is the default function of the client thread It decides
	 * which connection to connect to with
	 */
	public void run() {
		start = System.currentTimeMillis();
		try {
			if (connectionType.equals("-t")) {
				connectTCP();
			} else {
				connectUDP();
			}
		} catch (Exception e) {
		}
	}

	/*
	 * This function connects the client with the server/proxy using TCP It
	 * displays the time and other information sent by the server
	 */
	public void connectTCP() throws UnknownHostException, IOException {
		Socket sockClient = new Socket(serverAddress, port);
		
		for (int i = 0; i < this.numberOfQueries; i++) {
			try {
				OutputStream out = sockClient.getOutputStream();
				ObjectOutputStream objectOutput = new ObjectOutputStream(out);
				messageObject infoToServer = new messageObject(0, 0, username, password, setTime, utc, "",
						this.connectionType);
				start = System.currentTimeMillis();

				objectOutput.writeObject(infoToServer);
				objectOutput.flush();
				InputStream in = sockClient.getInputStream();
				ObjectInputStream oin = new ObjectInputStream(in);
				messageObject infoFromServer = (messageObject) oin.readObject();

				end = System.currentTimeMillis();
				totalRTT = (end - start);
				if (infoFromServer != null) {
					String utcFormat = "";
					if (infoFromServer.error.length() == 0) {
						System.out.println("Total Round Trip Time: " + totalRTT + " milliseconds");
						System.out.println("Time is: " + infoFromServer.time + " seconds");
						// converting to utc format
						if (infoFromServer.utc) {
							Date date = new Date(infoFromServer.time * 1000);

							DateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.US);
							format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

							utcFormat = format.format(date);
							System.out.println("UTC time requested: " + utcFormat);

						}

						System.out.println("Number of Hops: " + infoFromServer.noOfHops);
						System.out.println("Hop " + ("1") + " Address: " + infoFromServer.IPAddresses.get(0)
						+ " Time taken: " + (infoFromServer.timings.get(0) - start) + " milliseconds");

						for (int j = 1; j < infoFromServer.IPAddresses.size(); j++) {
							System.out.println("Hop " + (j+1) + " Address: " + infoFromServer.IPAddresses.get(j)
									+ " Time taken: " + (infoFromServer.timings.get(j) - infoFromServer.timings.get(j-1)) + " milliseconds");

						}
					} else {
						System.out.println(infoFromServer.error);
						System.exit(0);
					}

				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		sockClient.close();
	}

	/*************** UDP **********************/

	/*
	 * This function connects the client with the server/proxy using UDP It
	 * displays the time and other information sent by the server
	 */
	public void connectUDP() throws UnknownHostException, IOException {
		try {

			DatagramSocket clientSocket = new DatagramSocket();

			for (int i = 0; i < this.numberOfQueries; i++) {
				IPAddress = InetAddress.getByName(serverAddress);

				byte[] incomingData = new byte[1024];
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				ObjectOutputStream os = new ObjectOutputStream(outputStream);
				messageObject infoToServer = new messageObject(0,0, username, password, setTime, utc, "",
						this.connectionType);
				start = System.currentTimeMillis();

				os.writeObject(infoToServer);

				byte[] data = outputStream.toByteArray();
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
				clientSocket.send(sendPacket);

				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				clientSocket.receive(incomingPacket);

				byte[] fromServerData = incomingPacket.getData();

				ByteArrayInputStream in = new ByteArrayInputStream(fromServerData);
				ObjectInputStream ois = new ObjectInputStream(in);

				messageObject infoFromServer = (messageObject) ois.readObject();

				end = System.currentTimeMillis();
				totalRTT = (end - start);
				if (infoFromServer != null) {
					System.out.println("Total Round Trip Time: " + totalRTT + " milliseconds");
					if (infoFromServer.error.length() == 0) {
						String utcFormat = "";
						System.out.println("Time is: " + infoFromServer.time + " seconds");
						if (infoFromServer.utc) {
							Date date = new Date(infoFromServer.time * 1000);

							DateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.US);
							format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

							utcFormat = format.format(date);
							System.out.println("UTC time requested: " + utcFormat);

						}

						System.out.println("Number of Hops: " + infoFromServer.noOfHops);
						System.out.println("Hop " + ("1") + " Address: " + infoFromServer.IPAddresses.get(0)
						+ " Time taken: " + (infoFromServer.timings.get(0) - start) + " milliseconds");

						for (int j = 1; j < infoFromServer.IPAddresses.size(); j++) {
							System.out.println("Hop " + (j+1) + " Address: " + infoFromServer.IPAddresses.get(j)
									+ " Time taken: " + (infoFromServer.timings.get(j) - infoFromServer.timings.get(j-1)) + " milliseconds");

						}


					} else {
						System.out.println(infoFromServer.error);
						System.exit(0);
					}

				}

			}
			clientSocket.close();
			
		} catch (Exception e) {
		}
	}

}
