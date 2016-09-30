import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface to support two phase commit protocol
 */
public interface TwoPhaseCommit extends Remote {
	public static final String nameRes = "ReplicaServer";
	
	public ResponseTPC vote(String instruction,String key,String value) throws RemoteException;
	public void commit(String instruction,String key,String value) throws RemoteException, RollbackException;
	public void abort() throws RemoteException;
}
