import java.util.concurrent.ConcurrentHashMap;
/*
 * Backend database for the Key-Value Store.
 * It implements locking for the two phase commit.
 */
public class DataStore {

	private ConcurrentHashMap<String, String> data = new ConcurrentHashMap<String, String>();
		
	public boolean containsKey(String key) {
		return data.containsKey(key);
	}
	
	public String put(String key, String value) {
		return data.put(key, value);
	}
	
	public String remove(String key) {
		return data.remove(key);
	}
	
	public String get(String key) {
		return data.get(key);
	}
	
}
