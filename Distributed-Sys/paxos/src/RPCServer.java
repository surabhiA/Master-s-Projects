import java.io.FileOutputStream;
import java.io.IOException;
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
	 * @throws AlreadyBoundException 
	 * @throws IOException 
	 */
	public void start(int port, String[] hostnames, int uniqueID) throws AlreadyBoundException, IOException {		
		// create data store
		DataStore datastore = new DataStore();
		
		// creates objects to export
		Proposer proposer = new Proposer(uniqueID, hostnames); 
		Learner learner = new Learner(datastore, hostnames.length);
		Acceptor acceptor = new Acceptor(hostnames);		
		KVStoreImpl store = new KVStoreImpl(hostnames);					 
	
		ProposerInterface stubProposer = (ProposerInterface) UnicastRemoteObject.exportObject(proposer, port); // Creates stub of type Proposer and exports it on specified port		
		AcceptorInterface stubAcceptor = (AcceptorInterface) UnicastRemoteObject.exportObject(acceptor, port); // Creates stub of type Acceptor and exports it on specified port		
		LearnerInterface stubLearner = (LearnerInterface) UnicastRemoteObject.exportObject(learner, port); // Creates stub of type Learner and exports it on specified port
		KVStore stubStore = (KVStore) UnicastRemoteObject.exportObject(store, port); // Creates stub of type KVStore and exports it on specified port
		
		Registry registry;
		try {
			// create registry
			registry = LocateRegistry.createRegistry(1099);
		} catch (RemoteException remoteException) {
			// if one already exist, get it
			registry = LocateRegistry.getRegistry(1099);
		}
		
		registry.rebind(Acceptor.nameAcceptor, stubAcceptor);
		registry.rebind(Proposer.nameProposer, stubProposer);
		registry.rebind(Learner.nameLearner, stubLearner);
		registry.rebind(KVStore.nameRes, stubStore);	
		
		// set the log file to log the calls made to the RPC server
		FileOutputStream logFile = new FileOutputStream(logFileName);
		RemoteServer.setLog(logFile);
	}
	
	public static void main(String args[]) throws AlreadyBoundException, IOException{
		if(args.length < 3){
			System.out.println("Please provide server port number, unique ID and server hostnames as arguments");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		int uniqueID = Integer.parseInt(args[1]);
		RPCServer server = new RPCServer();
		String[] hostnames = Arrays.copyOfRange(args, 2, args.length);
		
		server.start(port, hostnames, uniqueID);
	}
}
