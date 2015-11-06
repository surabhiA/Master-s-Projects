import java.util.HashMap;

/**
 * This class creates a Key-Value Store which lasts for the duration that the server is listening
 * and defines the PUT , REMOVE and GET functions
 * @author Surabhi Agrawal
 */

public class KVStore {
	HashMap<String, String> map = new HashMap<String, String>();
	
	public String get(String key) {
		String value = map.get(key);
		return value;
	} 
	
	public void remove(String key) {
		map.remove(key);
	}
	
	public void put(String key,String value) {
		map.put(key,value);
	}
	
}
