import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface to support the Acceptor role of the paxos protocol
 */
public interface AcceptorInterface extends Remote {
	final static String nameAcceptor = "acceptor";
	
	// Function to call to prepare the acceptor during the phase 1
	Operation prepare(Operation oper) throws RemoteException, RejectedException;
	
	// Function to accept the final value during the phase 2
	void accept(Operation oper) throws RemoteException, RejectedException;

}