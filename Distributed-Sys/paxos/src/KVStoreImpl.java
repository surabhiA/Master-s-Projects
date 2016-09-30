import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Hashtable;
import java.util.Random;

/*
 * Implements the KVStore via paxos protocol
 */
public class KVStoreImpl implements KVStore{
	final int maxAttempts = 10; // number of attempts to find the proposer if previous ones fails	
	String[] hostnames; // the name of the servers

	public KVStoreImpl(String[] hostnames){
		this.hostnames = hostnames;	
	}

	@Override
	public void put(String key, String value) throws RemoteException {
		Operation oper = new Operation();
		oper.operation = OperationVerbs.PUT;
		oper.key = key;
		oper.value = value;	
		operate(oper);
	}
	
	/*
	 * Function to call the proposers to do the operation
	 */
	private void operate(Operation oper) {	
		int count = 0;
		Random rand = new Random();
		while (count < maxAttempts) {
			int i = rand.nextInt(hostnames.length);
			Registry registry;
			try {
				registry = LocateRegistry.getRegistry(hostnames[i]);
				ProposerInterface proposer = (ProposerInterface) registry.lookup(ProposerInterface.nameProposer);
				proposer.driveConcensus(oper);
				break;
			} catch (IOException | TimedOutException | NotBoundException e) {
				count++;
			}
		}
	}

	@Override
	public void delete(String key) throws KeyNotFoundException, RemoteException {
		Operation oper = new Operation();
		oper.operation = OperationVerbs.DELETE;
		oper.key = key;
		oper.value = null;
		
		if(!fContainsKey(key)) 
			throw new KeyNotFoundException();
		operate(oper);
	}

	private class Counter {
		int count;
	}
	
	@Override
	public synchronized String get(String key) throws KeyNotFoundException, TimedOutException {
		if(fContainsKey(key)) {
			Hashtable<String, Counter> results = new Hashtable<String, Counter>();
			LearnerInterface[] learners = new LearnerInterface[hostnames.length];

			for (int i = 0; i < hostnames.length; i++) {
				Registry registry;
				try {
					registry = LocateRegistry.getRegistry(hostnames[i]);
					learners[i] = (LearnerInterface) registry.lookup(LearnerInterface.nameLearner);
					String value = learners[i].get(key);
					if (value != null) {
						if (results.containsKey(value)) {
							results.get(value).count++;
						} else {
							Counter c = new Counter();
							c.count = 1;
							results.put(value, c);
						}
					}
				} catch (RemoteException | NotBoundException e) {
					//continue
				}
			}
			for (String possibleValue : results.keySet()) {
				if (results.get(possibleValue).count > hostnames.length/2)
					return possibleValue;
			}
		} else {
			throw new KeyNotFoundException();
		}
		throw new TimedOutException();
	}
	
	private synchronized boolean fContainsKey(String key) {
		int yesAnswer = 0;
		LearnerInterface[] learners = new LearnerInterface[hostnames.length];
		for (int i = 0; i < hostnames.length; i++) {
			Registry registry;
			try {
				registry = LocateRegistry.getRegistry(hostnames[i]);
				learners[i] = (LearnerInterface) registry.lookup(LearnerInterface.nameLearner);
				if (learners[i].containsKey(key))
					yesAnswer++;
			} catch (RemoteException | NotBoundException e) {
				//continue
			}
		}
		return yesAnswer > hostnames.length/2;
	}
}
