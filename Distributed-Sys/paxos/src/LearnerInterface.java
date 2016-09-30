import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface to the learner role of the paxos protocol
 */
public interface LearnerInterface extends Remote{	
	public final static String nameLearner = "Learner";

	// Function called by acceptors to learn the final accepted value.
	void learn(Operation oper) throws RemoteException;
	
	// Function to access the datastore for get functionality
	String get(String key) throws RemoteException;
	boolean containsKey(String key) throws RemoteException;
}