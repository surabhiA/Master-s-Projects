import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class RPCServer {
	private final String logFileName="server.log";
	{
		// enable server side transport level logging. Does not output to screen (false)
		System.setProperty("java.rmi.server.logCalls","false");
		System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
	}
	
	
	/**
	 * Starts the server
	 * @param port
	 * @throws RemoteException 
	 * @throws AlreadyBoundException 
	 * @throws FileNotFoundException 
	 */
	public void start(int port, String[] hostnames) throws RemoteException, MalformedURLException, AlreadyBoundException, FileNotFoundException {		
		// create data store
		DataStore datastore = new DataStore();
		
		// create TPCCordinator
		TPCCoordinator coordinator = new TPCCoordinator(hostnames); 
		
		// creates objects to export
		TwoPhaseCommitImpl twoPhaseImpl = new TwoPhaseCommitImpl(datastore);
		TwoPhaseCommit stubTwoPhaseImpl = (TwoPhaseCommit) UnicastRemoteObject.exportObject(twoPhaseImpl, port);
		
		KVStore store = new KVStoreImpl(coordinator, datastore);					 // Creates object of type KVStore
		KVStore stubStore = (KVStore) UnicastRemoteObject.exportObject(store, port); // Creates stub of type KVStore and exports it on specified port
		
		Registry registry;
		try {
			// create registry
			registry = LocateRegistry.createRegistry(1099);
		} catch (RemoteException remoteException) {
			// if one already exist, get it
			registry = LocateRegistry.getRegistry(1099);
		}
		registry.rebind(TwoPhaseCommit.nameRes, stubTwoPhaseImpl);
		registry.rebind(KVStore.nameRes, stubStore);	
		
		// set the log file to log the calls made to the RPC server
		FileOutputStream logFile = new FileOutputStream(logFileName);
		RemoteServer.setLog(logFile);
	}
	
	public static void main(String args[]) throws AlreadyBoundException, RemoteException, MalformedURLException, FileNotFoundException{
		if(args.length < 2){
			System.out.println("Please provide server port number and server hostnames as arguments");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		RPCServer server = new RPCServer();
		String[] hostnames = Arrays.copyOfRange(args, 1, args.length);
		
		server.start(port, hostnames);
	}
}
