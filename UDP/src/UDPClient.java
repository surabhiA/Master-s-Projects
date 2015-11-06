import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/**
 * This class initializes the UDP Client by taking the server IP and server port number as command line arguments.
 * It populates the hash table by sending requests to the server.
 * It calls the KVRequest class to obtain the query in serializable format.
 * It then serializes the query sends it to the server. 
 * It then waits for a response from the server and deserializes it on receiving.
 * It handles exceptions like server timeout and unrequested and malformed packets received.
 * @author Surabhi Agrawal
 */


public class UDPClient {
	InetAddress serverIP;
	int port;
	DatagramSocket socket; //binds it to any available port
	Logger logclient;
	
	public UDPClient(String servername, int port) throws Exception {
		this.port = port;
		this.serverIP = InetAddress.getByName(servername);		//converts the named server address to IP
		socket = new DatagramSocket();
		socket.setSoTimeout(1000);								//sets server timeout
		logclient = new Logger("../src/ClientLog.txt");			//Specifies the client log file on the local system
	}
	
	/**
	 * This function sets the put query in serializable format.
	 * @param key : Key to be pushed into the KVStore
	 * @param value : Corresponding value to be pushed
	 */
	
	public void put(String key, String value) {
		KVRequest m = new KVRequest("put", key, value);
		sendMessage(m);
		receiveMessage(m);
	}
	
	/**
	 * This function sets the query in serializable format.
	 * @param key : key of the key-value pair to be removed
	 */
	
	public void remove(String key) {
		KVRequest m = new KVRequest("remove", key);
		sendMessage(m);
		receiveMessage(m);
	}
	
	/**
	 * This function sets the query in serializable format.
	 * @param key : The key corresponding to the value to be obtained
	 */
	
	public void get(String key) {
		KVRequest m = new KVRequest("get", key);
		sendMessage(m);
		receiveMessage(m);
	}
	
	/**
	 * This function serializes the query and sends it to the server
	 * @param message : The query in serializable format
	 */
	
	public void sendMessage(KVRequest message) {
		try{
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(b);
			o.writeObject(message);
			byte[] sendbuffer = b.toByteArray();
			DatagramPacket sendpacket = new DatagramPacket(sendbuffer, sendbuffer.length, serverIP, port);
			socket.send(sendpacket);
		} catch(Exception e) {
			logclient.addError(message, "Exception occured " + e.getMessage());
		}
		
	}
	
	/**
	 * This function receives the response to the query sent
	 * It deserializes the response received and prints it
	 * It also handles the exception occured if the packet received was not requested, i.e the keys of the query and response do not match
	 * and then logs it to the client log
	 * It handles the SocketTimeoutException if the server is unresponsive
	 * It handles the exception if the response received is not in a valid format, i.e malformed response and logs it to the client log
	 */
	
	public void receiveMessage(KVRequest m) {
		
		
		byte[] receivebuffer = new byte[1000];
		DatagramPacket receivedpacket = new DatagramPacket(receivebuffer,
				receivebuffer.length);
		
		try {
			socket.receive(receivedpacket);
			ByteArrayInputStream b = new ByteArrayInputStream(receivedpacket.getData());
			ObjectInputStream i = new ObjectInputStream(b);
			KVResponse r = (KVResponse) i.readObject();
			
			if(!m.getKey().equals(r.getRequestedKey())){
				System.out.println("Unrequested packet with invalid key.");
				logclient.addError(receivedpacket.getAddress(), receivedpacket.getPort(), "Unrequested packet with invalid key.");
			}
			else
				System.out.println(r.toString());
			
		} catch (SocketTimeoutException e) {
			logclient.addError(m, "Timeout error occurred.");
			System.out.println("Timeout Exception occurred");
		} catch(StreamCorruptedException e){
			System.out.println("Malformed response");
			logclient.addError(receivedpacket.getAddress(), receivedpacket.getPort(), "Malformed response received from server");
		} catch (IOException e) {
			logclient.addError(m, "IOException occured " + e.getMessage());
		} catch (Exception e){
			logclient.addError(m, "Exception occured " + e.getMessage());
		}

	}
	
	public void destroy(){
		socket.close();
		logclient.close();
	}

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Please provide server name and port number as arguments");
			return;
		}
		
		int port = Integer.parseInt(args[1]);
		
		try{
			UDPClient c = new UDPClient(args[0],port);
			
			c.put("New Delhi","India");		//Pre-populating the KVStore
			c.put("Texas","USA");
			c.put("Washington","USA");
			c.put("Rajasthan","India");
			c.put("London","UK");
			
			Scanner reader = new Scanner(System.in);  // Reading from System.in
			String ans;
			String instruction;
			String key;
			String value;
			
			while(true){
				
				System.out.println("Please enter the instruction to be executed on the KVStore.Type exit to quit client: ");
				instruction = reader.nextLine();
				
				if(instruction.toUpperCase().equals("PUT")){
					System.out.println("Please enter the key ");
					key = reader.nextLine();
					System.out.println("Please enter the value ");
					value = reader.nextLine();
					c.put(key,value);
				}
				else if(instruction.toUpperCase().equals("GET")){
					System.out.println("Please enter the key ");
					key = reader.nextLine();
					c.get(key);
				}
				else if(instruction.toUpperCase().equals("REMOVE")){
					System.out.println("Please enter the key ");
					key = reader.nextLine();
					c.remove(key);
				}
				else if(instruction.toUpperCase().equals("EXIT")){
					break;
				}
				else{
					System.out.println("Incorrect format.Please enter only the instruction name Eg: put");
				}
			}
			c.destroy();
		} catch(Exception e) {
			System.out.println("Exception occured." + e.getMessage());
		}
		
		
	}

}
