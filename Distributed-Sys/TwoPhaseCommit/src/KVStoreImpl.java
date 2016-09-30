import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/*
 * Simple in-memory Key Value store
 * The functions are synchronized to enable multithreaded access.
 */
public class KVStoreImpl implements KVStore{
	
	private TPCCoordinator twoPhaseCommit;
	private DataStore data;

	public KVStoreImpl(TPCCoordinator peerCordinator, DataStore data ){
		this.twoPhaseCommit = peerCordinator;
		this.data = data;
	}

	@Override
	public void put(String key, String value) throws RemoteException {
		try {
			twoPhaseCommit.commit("put", key, value);
		}
		catch (AccessException | NotBoundException e) {
			throw new RemoteException("Two Phase Commit error", e);
		}
	}

	@Override
	public void delete(String key) throws KeyNotFoundException, RemoteException {
		if(!data.containsKey(key)) 
			throw new KeyNotFoundException();
		try {
			twoPhaseCommit.commit("remove", key, null);
		}
		catch (AccessException | NotBoundException e) {
			throw new RemoteException("Two Phase Commit error", e);
		}
	}

	@Override
	public synchronized String get(String key) throws KeyNotFoundException {
		if(data.containsKey(key)){
			String value = data.get(key);
			return value;
		} else {
			throw new KeyNotFoundException();
		}
	}	
}
