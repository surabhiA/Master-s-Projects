import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 * This class handles the Two Phase commit protocol as a coordinator.
 */
public class TPCCoordinator {
	private TwoPhaseCommit[] peerObjects;
	private String[] hostnames;
	
	public TPCCoordinator(String[] servers) {
		this.hostnames = servers;
		peerObjects = new TwoPhaseCommit[servers.length];
	}
	
	public void commit(String instruction,String key,String value) throws NotBoundException, AccessException, RemoteException {
		// get peers
		for (int i = 0; i < hostnames.length; i++) {
			Registry registry = LocateRegistry.getRegistry(hostnames[i]);
			peerObjects[i] = (TwoPhaseCommit) registry.lookup(TwoPhaseCommit.nameRes);
			}
	
		// voting phase
		System.out.println("Starting voting");
		boolean fVoted = true; // true means that all peers as yet agree.
		for (int i = 0; i < hostnames.length; i++) {
			try {
				ResponseTPC response = peerObjects[i].vote(instruction, key, value) ;
				if(response != ResponseTPC.ACK) {
					System.out.println(hostnames[i] + " returned reponse " + response );
					fVoted = false;
				}
			} catch (RemoteException e) {
				System.out.println("Exception while voting by " + hostnames[i]);
				fVoted = false;
			}
		}
		System.out.println("Ending  voting with result = " + fVoted);

		// commit phase
		if (fVoted) {
			System.out.println("Starting commiting.");
			for (int i = 0; i < hostnames.length; i++) {
				try {
					peerObjects[i].commit(instruction, key, value);
				} catch (RollbackException | RemoteException e) {
					// two phase commit protocol assumes that the commit phase will always succeed.
					// otherwise sending an abort message will cause the peers to rollback their transaction
				}
			}
		}
		
		// abort phase.
		if (!fVoted) {
			System.out.println("Aborting the commit");
			for (int i = 0; i < hostnames.length; i++) {
				try {
					peerObjects[i].abort();
				} catch (RemoteException e) {
				}
			}
		}
	} 

}
