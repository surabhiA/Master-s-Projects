import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
	
	private KVStore store;
	private final String logFileName="client.log";
	private Logger log;
		
	/*
	 * A simple console UI
	 */
	private void consoleUI(){
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		String ans;
		String instruction;
		String key;
		String value;
		
		while(true){
			
			System.out.println("Please enter the instruction to be executed on the KVStore. Type exit to quit client: ");
			instruction = reader.nextLine();
			
			if(instruction.toUpperCase().equals("PUT")) {
				System.out.println("Please enter the key ");
				key = reader.nextLine();
				System.out.println("Please enter the value ");
				value = reader.nextLine();
				try {
					store.put(key,value);
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
					ans = store.get(key);
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
					store.delete(key);
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
	public void start(String hostname) throws NotBoundException, SecurityException, IOException, KeyNotFoundException {		
		Registry registry = LocateRegistry.getRegistry(hostname);
		store = (KVStore) registry.lookup(KVStore.nameRes);
		
		prePopulateValues();
	
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
		store.put("Key1", "value1");
		store.put("Key2", "value2");
		store.put("Key3", "value3");
		store.put("Key4", "value4");
		store.put("Key5", "value5");
		store.get("Key1");
		store.get("Key2");
		store.get("Key3");
		store.get("Key4");
		store.get("Key5");
		store.delete("Key1");
		store.put("Key1", "Value1");
		store.delete("Key2");
		store.put("Key2", "Value2");
		store.delete("Key3");
		store.put("Key3", "Value3");
		store.delete("Key4");
		store.put("Key4", "Value4");
		store.delete("Key5");
		store.put("Key5", "Value5");
	}

	public static void main(String args[]) throws NotBoundException, SecurityException, IOException, KeyNotFoundException{
		if(args.length != 1) {
			System.out.println("Please provide server name as argument");
			return;
		}
		
		String serverName = args[0];
		RPCClient client = new RPCClient();
				
		client.start(serverName);
	}
	
}
