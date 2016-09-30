import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class RPCClient {
	{
		// set up the formatter for logging
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tc %4$s: %5$s%6$s%n");
	}
	
	private final String logFileName="client.log";
	private Logger log;
	private KVStore[] serverObjects;
	private String[] hostnames;
	
		
	/*
	 * A simple console UI
	 */
	private void consoleUI(){
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		String ans;
		String instruction;
		String key;
		String value;
		int chosenIndex;
		
		while(true){
			chosenIndex = chooseRandomServer();
			System.out.println("\nChosen Server: " + hostnames[chosenIndex]);
			
			System.out.println("Please enter the instruction to be executed on the KVStore. Type exit to quit client: ");
			instruction = reader.nextLine();
			
			if(instruction.toUpperCase().equals("PUT")) {
				System.out.println("Please enter the key ");
				key = reader.nextLine();
				System.out.println("Please enter the value ");
				value = reader.nextLine();
				try {
					serverObjects[chosenIndex].put(key,value);
				}
				catch(RemoteException e) {
					System.out.println("Error happened");
					log.log(Level.SEVERE, "PUT instruction failed : Key : " + key + "  Value: " + value, e);
				}
			}
			else if(instruction.toUpperCase().equals("GET")) {
				System.out.println("Please enter the key ");
				key = reader.nextLine();
				try {
					ans = serverObjects[chosenIndex].get(key);
					System.out.println("Returned: " + ans);
				}
				catch (KeyNotFoundException e) {
					System.out.println("Key not found");
					log.log(Level.INFO, "GET instruction failed : Key = " + key + " not found");
				}
				catch(RemoteException e) {
					System.out.println("Error happened");
					log.log(Level.SEVERE, "GET instruction failed : Key : " + key, e);
				}
			}
			else if(instruction.toUpperCase().equals("DELETE")){
				System.out.println("Please enter the key ");
				key = reader.nextLine();
				try {
					serverObjects[chosenIndex].delete(key);
				}
				catch (KeyNotFoundException e) {
					System.out.println("Key not found");
					log.log(Level.INFO, "DELETE instruction failed : Key = " + key + " not found");
				}
				catch(RemoteException e) {
					System.out.println("Error happened");
					log.log(Level.SEVERE, "DELETE instruction failed : Key : " + key, e);
				}
			}
			else if(instruction.toUpperCase().equals("EXIT")){
				reader.close();
				break;
			}
			else{
				System.out.println("Incorrect instruction. Please enter PUT, GET, DELETE or EXIT");
			}
			
		}
	}
	
	/**
	 * Starts the Client
	 * @param hostname
	 * @throws NotBoundException
	 * @throws IOException 
	 * @throws SecurityException 
	 * @throws KeyNotFoundException 
	 */
	public void start(String[] hostnames) throws NotBoundException, SecurityException, IOException, KeyNotFoundException {
		//Get all Server stubs
		this.hostnames = hostnames; 
		serverObjects = new KVStore[hostnames.length];
		for(int i = 0; i < hostnames.length;i++){
			Registry registry = LocateRegistry.getRegistry(hostnames[i]);
			serverObjects[i] = (KVStore) registry.lookup(KVStore.nameRes);
		}
		
		prePopulateValues();
		
		//Set up Logging
		log = Logger.getLogger("client");
		log.setUseParentHandlers(false);		
		FileHandler handler = new FileHandler(logFileName, true); //append to log
		SimpleFormatter formatter = new SimpleFormatter();
		handler.setFormatter(formatter);
		log.addHandler(handler);
		
		consoleUI();
	}
	
	/*
	 * Prepopulate some data in the server. Also run some sample commands.
	 */
	private void prePopulateValues() throws RemoteException, KeyNotFoundException {
		System.out.println("Performing 10 puts,5 gets,5 deletes...");
		
		serverObjects[chooseRandomServer()].put("Key1", "value1");
		serverObjects[chooseRandomServer()].put("Key2", "value2");
		serverObjects[chooseRandomServer()].put("Key3", "value3");
		serverObjects[chooseRandomServer()].put("Key4", "value4");
		serverObjects[chooseRandomServer()].put("Key5", "value5");
		serverObjects[chooseRandomServer()].get("Key1");
		serverObjects[chooseRandomServer()].get("Key2");
		serverObjects[chooseRandomServer()].get("Key3");
		serverObjects[chooseRandomServer()].get("Key4");
		serverObjects[chooseRandomServer()].get("Key5");
		serverObjects[chooseRandomServer()].delete("Key1");
		serverObjects[chooseRandomServer()].put("Key1", "Value1");
		serverObjects[chooseRandomServer()].delete("Key2");
		serverObjects[chooseRandomServer()].put("Key2", "Value2");
		serverObjects[chooseRandomServer()].delete("Key3");
		serverObjects[chooseRandomServer()].put("Key3", "Value3");
		serverObjects[chooseRandomServer()].delete("Key4");
		serverObjects[chooseRandomServer()].put("Key4", "Value4");
		serverObjects[chooseRandomServer()].delete("Key5");
		serverObjects[chooseRandomServer()].put("Key5", "Value5");
	}
	
	public int chooseRandomServer(){
		Random r = new Random();
		int chosenIndex = r.nextInt(serverObjects.length);
		return chosenIndex;
	} 

	public static void main(String args[]) throws NotBoundException, SecurityException, IOException, KeyNotFoundException{
		if(args.length == 0) {
			System.out.println("Please provide all servernames/serverIPs as arguments");
			return;
		}

		RPCClient client = new RPCClient();

		client.start(args);
	}
	
}
