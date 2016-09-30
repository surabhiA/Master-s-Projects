import java.rmi.RemoteException;

/*
 * This class implements the learner role of the paxos protocol
 * Only the learners have access to the actual datastore where the data is stored
 */
public class Learner implements LearnerInterface {
	private DataStore datastore; // in-memory data store 
	int maxAcceptors; // maximum number of acceptors.
	int countAcceptors = 0; // count of acceptors that have sent us the value
	Operation cache = null; // the value sent
	
	Learner(DataStore datastore, int maxAcceptors) {
		this.datastore = datastore;
		this.maxAcceptors = maxAcceptors;
	}

	/*
	 * (non-Javadoc)
	 * @see LearnerInterface#learn(Operation)
	 */
	@Override
	public synchronized void learn(Operation oper) throws RemoteException {
		if (oper.equals(cache)) {
			countAcceptors++;
		} else {
			cache = oper;
			countAcceptors = 1;
		}
		
		if (countAcceptors > maxAcceptors/2) {
			switch (oper.operation) {
			case DELETE:
				if (datastore.containsKey(oper.key))
					datastore.remove(oper.key);
				break;
			case PUT:
				datastore.put(oper.key, oper.value);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public String get(String key) throws RemoteException {
		return datastore.get(key);
	}

	@Override
	public boolean containsKey(String key) throws RemoteException {
		return datastore.containsKey(key);
	}
	
	
}
