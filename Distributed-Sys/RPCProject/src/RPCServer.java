import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

public class RPCServer {

	private final String logFileName="server.log";

	{
		// enable server side transport level logging. Does not output to screen (false)
		System.setProperty("java.rmi.server.logCalls","false");
	}
	
	/**
	 * Starts the server
	 * @param port
	 * @throws RemoteException 
	 * @throws AlreadyBoundException 
	 * @throws FileNotFoundException 
	 */
	public void start(int port) throws RemoteException, MalformedURLException, AlreadyBoundException, FileNotFoundException {
		// set the log file to log the calls made to the RPC server
		FileOutputStream logFile = new FileOutputStream(logFileName);
		RemoteServer.setLog(logFile);

		KVStore store = new KVStoreImpl();									  // Creates object of type KVStore
		KVStore stub = (KVStore) UnicastRemoteObject.exportObject(store,port);// Creates stub of type KVStore and exports it on specified port
		Registry registry;
		try {
			// create registry
			registry = LocateRegistry.createRegistry(1099);
		} catch (RemoteException remoteException) {
			// if one already exist, get it
			registry = LocateRegistry.getRegistry(1099);
		}
		registry.rebind(KVStore.nameRes, stub);	
	}
	
	public static void main(String args[]) throws AlreadyBoundException, RemoteException, MalformedURLException, FileNotFoundException{
		if(args.length != 1){
			System.out.println("Please provide server port number as argument");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		RPCServer server = new RPCServer();
		server.start(port);
	}
}
