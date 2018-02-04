import java.util.*;
import java.io.*;

public class tsapp {
	String connectionType = "-u";
	int setTime;
	String username = "";
	String password = "";
	int numberOfQueries = 1;
	String serverAddress = "";
	String timeUTC = "";
	boolean portGiven = false;
	boolean TCPpresent = false;
	int port;
	int alternatePort;
	int proxyTCP = 0;
	int proxyUDP = 0;

	/*
	 * @param args[] This has all the values needed by the client This function
	 * creates a client thread, initializing all the variables required by the
	 * client
	 *
	 */
	public void createClient(String args[]) {
		// if more options given
		if (args.length > 1) {
			for (int i = 1; i < args.length;) {
				if (args[i].equals("-u") || args[i].equals("-t")) {
					connectionType = args[i];

				} else if (args[i].contains("-T")) {
					setTime = Integer.parseInt(args[i + 1]);
					i++;
				} else if (args[i].contains("--user")) {
					username = args[i + 1];
					i++;

				} else if (args[i].contains("--pass")) {
					password = args[i + 1];
					i++;

				} else if (args[i].contains("-z")) {
					timeUTC = args[i];
				} else if (args[i].contains("-n")) {
					numberOfQueries = Integer.parseInt(args[i + 1]);
					i++;

				} else if (args[i].contains(".")) {
					serverAddress = args[i];

				} else if (args[i].length() == 4 && portGiven == false) {
					port = Integer.parseInt(args[i]);
					portGiven = true;
				}
				i++;
			}
		}

		validate("client");
		System.out.println("Client!");
		new client(connectionType, setTime, username, password, timeUTC, numberOfQueries, serverAddress, port).start();

	}

	/*
	 * @param args[] This has all the values needed by the server This function
	 * creates a server thread, initializing all the variables required by the
	 * server
	 *
	 */
	public void createServer(String args[]) {

		// if more options given
		if (args.length > 1) {
			for (int i = 1; i < args.length;) {
				if (args[i].contains("-T")) {
					setTime = Integer.parseInt(args[i + 1]);
					i++;
				} else if (args[i].contains("--user")) {
					username = args[i + 1];
					i++;
				} else if (args[i].contains("-pass")) {
					password = args[i + 1];
					i++;

				} else if (args[i].contains(".")) {
					serverAddress = args[i];
				} else if (args[i].length() == 4 && portGiven == false) {
					port = Integer.parseInt(args[i]);
					portGiven = true;
				} else if (args[i].length() == 4 && portGiven == true) {
					alternatePort = Integer.parseInt(args[i]);
				}
				i++;
			}
		}

		validate("server");
		System.out.println("Sever!");
		new server(setTime, username, password, port, alternatePort).start();
	}

	/**** proxy creation ***/
	/*
	 * @param args[] This has all the values needed by the proxy This function
	 * creates a proxy thread, initializing all the variables required by the
	 * proxy
	 *
	 */
	public void createProxy(String args[]) {

		// if more options given
		if (args.length > 1) {
			connectionType = "";
			for (int i = 1; i < args.length;) {
				if (args[i].equals("-u") || args[i].equals("-t")) {
					connectionType = args[i];
				} else if (args[i].contains("--proxy-udp")) {
					proxyUDP = Integer.parseInt(args[i + 1]);
					i++;
				} else if (args[i].contains("--proxy-tcp")) {
					proxyTCP = Integer.parseInt(args[i + 1]);
					TCPpresent = true;
					i++;
				} else if (args[i].contains(".")) {
					serverAddress = args[i];
				} else if (args[i].length() == 4 && portGiven == false) {
					port = Integer.parseInt(args[i]);
					portGiven = true;
				} else if (args[i].length() == 4 && portGiven == true) {
					alternatePort = Integer.parseInt(args[i]);
				}
				i++;
			}
		}
		validate("proxy");
		System.out.println("Proxy!");

		try {
			new proxy(connectionType, proxyUDP, proxyTCP, port, alternatePort, serverAddress, TCPpresent).start();

		} catch (Exception e) {
		}

	}

	/*
	 * @param args[] This has all the values needed to decide client,server and
	 * proxy This function creates a client,server or proxy based on the user
	 * input
	 *
	 */
	public static void main(String args[]) {
		tsapp mainObject = new tsapp();
		if (args.length == 0) {
			System.out.println("Oops! Specify the operation: Run as client -c\n Run as Server -s \n Run as Proxy -p");
			System.exit(0);
		} else {
			if (args[0].equals("-c") || args[0].equals("-s") || args[0].equals("-p")) {
				// for client
				if (args[0].equals("-c")) {
					mainObject.createClient(args);
				}
				// for server
				else if (args[0].equals("-s")) {
					mainObject.createServer(args);
				}
				// for proxy
				else {
					mainObject.createProxy(args);
				}
			} else {
				System.out
						.println("Oops! Specify the operation: Run as client -c\n Run as Server -s \n Run as Proxy -p");
				System.exit(0);
			}
		}
	}

	/***************** Validations ***************************/
	/*
	 * @param name This variable decides the operator type
	 * This function validates all the information provided.
	 * Checks if the port, username, etc are valid and 
	 * if mandatory fields are provided by the user.
	 *
	 */

	public void validate(String name) {

		// default port for all
		if (port == 0) {
			System.out.println("Oops! You need to enter the port number.");
			System.exit(0);
		}

		// require username and password to set the time
		if (name.equals("client"))
			if (setTime != 0) {
				if (username.length() == 0 || password.length() == 0) {
					System.out.println("Oops! You need to enter User name and Password inorder to set the time");
					System.exit(0);
				}
			}

		// missing server address
		if (name.equals("proxy") || name.equals("client")) {

			if (serverAddress.length() == 0) {
				System.out.println("Oops! Specify the server address.");
				System.exit(0);
			}

		}
		// ports compulsory
		if (name.equals("proxy") || name.equals("server")) {
			if (port == 0 || alternatePort == 0) {
				System.out.println("Oops! Specify the ports.");
				System.exit(0);
			}

		}
		//if operator is server, check if the time is given or not
		if (name.equals("server")) {
			if (setTime == 0) {
				System.out.println("Oops! Specify the time.");
				System.exit(0);
			}

		}
		//if the operator is proxy,check for connection type and if value is given
		if (name.equals("proxy")) {
			if (connectionType.equals("-t")) {
				if (proxyTCP == 0) {
					System.out.println("Oops! Specify the TCP port.");
					System.exit(0);
				}

				if (connectionType.equals("-u")) {
					if (proxyUDP == 0) {
						System.out.println("Oops! Specify the UDP port.");
						System.exit(0);
					}
				}
			}

		}

	}
}
