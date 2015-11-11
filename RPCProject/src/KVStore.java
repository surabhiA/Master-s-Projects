import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Interface of a simple Key Value Store. Null values are not allowed.
 */
public interface KVStore extends Remote {	
	public static final String nameRes = "KVStore";
	
	public void put(String key, String value) throws RemoteException;
	public void delete(String key) throws RemoteException, KeyNotFoundException;
	public String get(String key) throws RemoteException, KeyNotFoundException;
}
