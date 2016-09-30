import java.util.concurrent.ConcurrentHashMap;
/*
 * Backend database for the Key-Value Store.
 * It implements locking for the two phase commit.
 */
public class DataStore {

	private ConcurrentHashMap<String, String> data = new ConcurrentHashMap<String, String>();
	private boolean fInTwoPhaseCommit = false;
	private long lockStartTime;
	
	public DataStore(){
		LockManager lockManager = new LockManager();
		lockManager.start();
	}
	
	/*
	 * This class runs a background thread which prevents the peer from hanging in case 
	 * the coordinator didn't send the commit or abort message.
	 */
	public class LockManager extends Thread {
		public void run(){
			while(true){
				if(fInTwoPhaseCommit && System.currentTimeMillis() - lockStartTime >= 10000){
					endTwoPhaseCommit();
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public synchronized void startTwoPhaseCommit() {
		while(this.fInTwoPhaseCommit);
		this.fInTwoPhaseCommit = true;
		lockStartTime = System.currentTimeMillis();
	}

	public synchronized void endTwoPhaseCommit() {
		this.fInTwoPhaseCommit = false;
	}
	
	public boolean containsKey(String key) {
		return data.containsKey(key);
	}
	
	public void put(String key, String value) {
		data.put(key, value);
	}
	
	public void remove(String key) {
		data.remove(key);
	}
	
	public String get(String key) {
		return data.get(key);
	}
	
}
