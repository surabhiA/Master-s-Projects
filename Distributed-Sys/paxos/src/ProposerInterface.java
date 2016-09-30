import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * The interface to the Proposer role in paxos protocol
 */
public interface ProposerInterface extends Remote {
	final static String nameProposer  = "Proposer";

	void driveConcensus(Operation oper) throws IOException, TimedOutException, RemoteException;
}