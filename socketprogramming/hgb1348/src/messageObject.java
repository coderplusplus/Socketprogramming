
import java.io.*;
import java.net.InetAddress;
import java.util.*;

/*
 * This class is used to generate the message to be sent between the client and the server
 */
public class messageObject implements Serializable {
	int noOfHops;
	int type;
	int time;
	String username;
	String password;
	int setTime;
	boolean utc;
	List<String> IPAddresses = new ArrayList<String>();
	List<Long> timings = new ArrayList<Long>();
	String error = "";
	String clientConnectionType;

	/*
	 * This method initializes all the values of the message needed by the
	 * server and the client
	 */
	public messageObject(int hops, int time, String user, String password, int setTime, boolean utc,
			String error, String clientConnectionType) {
		this.noOfHops = hops;
		this.time = time;
		this.username = user;
		this.password = password;
		this.setTime = setTime;
		this.utc = utc;
		this.error = error;
		this.clientConnectionType = clientConnectionType;

	}
}
