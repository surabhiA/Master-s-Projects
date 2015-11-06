import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This class creates a UDP Server and listens infinitely for incoming messages
 * It takes the port number as command line argument
 * When a request is received, it de-serializes the KVRequest using the KVRequest class functions
 * It sends the response back to the client by serializing it by using the KVResponse class
 * It logs all queries received and its responses in a server log by calling the Logger class
 * It recognizes malformed packets and displays a message saying "Malformed request" and logs it to the server log
 * It calls the KVStore class and performs operations on the hash table
 * @author Surabhi Agrawal
 */

public class UDPServer {
	int port;
	DatagramSocket socket;
	KVStore hashtable;
	Logger log;
	
	public UDPServer(int port) throws IOException {
		this.port = port;
		socket = new DatagramSocket(port);
		hashtable = new KVStore();
		log = new Logger("ServerLog.txt");  	//Specifies the server log file location on the system
	}
	
	/**
	 * The sendData() function serializes the response and sends it to the client
	 * @param datasent : The response to be sent to client. Exists in serializable format performed by KVResponse class
	 * @param sourceIP : IP of the client
	 * @param sourcePort : port number of the client
	 */
	
	public void sendData(KVResponse datasent, InetAddress sourceIP, int sourcePort) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(datasent);
		byte[] sendbuffer = b.toByteArray();
		DatagramPacket sendpacket = new DatagramPacket(sendbuffer, sendbuffer.length, sourceIP, sourcePort);
		socket.send(sendpacket);
	}
	
	/**
	 * This function processes the request received from the client by 1st extracting the values from the serialized format using KVRequest class
	 * It performs the required operations by calling the KVStore class's functions
	 * Then it generates the response to the query in serializable format using KVResponse class
	 * @param m : The query received from the client as an object of KVRequest class
	 */
	
	public KVResponse processData(KVRequest m) {
		
		String getvalue;
		
		String instruction = m.getInstruction();
		String key = m.getKey();
		String value = m.getValue();
		KVResponse r;
		
		if (instruction.equals("put")){
			hashtable.put(key,value);
			r = new KVResponse(key, "put instruction successful.");
			return r;
		}
		else if(instruction.equals("remove")){
			if (hashtable.get(key) == null){
				r = new KVResponse(key, "Remove unsuccessful. Key does not exist.");
				return r;
			}
			else {
				hashtable.remove(key);
				r = new KVResponse(key, "remove instruction successful.");
				return r;
			}
			
		}
		else if(instruction.equals("get")) {
			if(hashtable.get(key) == null){
				r = new KVResponse(key, "get unsuccessful. Key does not exist.");
				return r;
			}
			else{
				getvalue = hashtable.get(key);
				r = new KVResponse(key, getvalue, "get successful.");
				return r;
			}
		}
		else {
			r = new KVResponse(key, "Unrecognised operation.");
			return r;
		}
		
	}
	
	
	/**
	 * This function listens continuously for incoming requests.
	 * On receiving a request, it deserializes the request and sends it for further processing
	 * It then sends the response to the client and adds everything to the server log
	 * It also handles the Malformed request exception and adds it to the log if occured
	 */
	
	public void startServer() {
		while (true) {
			byte[] receivebuffer = new byte[1000];
			DatagramPacket receivedpacket = new DatagramPacket(receivebuffer, receivebuffer.length);
			
			try{
				socket.receive(receivedpacket);
				ByteArrayInputStream b = new ByteArrayInputStream(receivedpacket.getData());
				ObjectInputStream i = new ObjectInputStream(b);
				KVRequest m = (KVRequest) i.readObject();
				
				KVResponse datasent = processData(m);
				sendData(datasent,receivedpacket.getAddress(),receivedpacket.getPort());	
				log.add(receivedpacket.getAddress(), receivedpacket.getPort(), m, datasent);
				
			} catch(StreamCorruptedException e){
				System.out.println("Malformed Request");
				log.addError(receivedpacket.getAddress(), receivedpacket.getPort(),"Malformed request received from client");
			} catch(IOException e) {
				log.addError(receivedpacket.getAddress(), receivedpacket.getPort(), "IOException occured" + e.getMessage());
			} catch(ClassNotFoundException e) {
				log.addError(receivedpacket.getAddress(), receivedpacket.getPort(), "ClassNotFoundException occured" + e.getMessage());
			} catch(Exception e) {
				log.addError(receivedpacket.getAddress(), receivedpacket.getPort(), "Exception occured" + e.getMessage());
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		if(args.length != 1){
			System.out.println("Please provide server port number as argument");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		
		try{
			UDPServer s = new UDPServer(port);		//Initializes the server by binding the socket to the specified port
			s.startServer();
		}catch(Exception e){
			System.out.println("Server could not start " + e.getMessage());
		}
				
	}
}
