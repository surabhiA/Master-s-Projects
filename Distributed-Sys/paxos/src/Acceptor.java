import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 * This class implements the Acceptor role of the paxos protocol
 * Each server has acceptors which can fail randomly due to gremlins.
 */
public class Acceptor implements AcceptorInterface {
	private long currentN; 	// the current request number that is being accepted.
	private Operation lastOper; // the last request which was promised by the acceptor
	private final String filename = "acceptor.bak"; // backup file to store values in case of power loss 
	
	String[] hostnames; // name of servers
	LearnerInterface[] learners; // cache of Learners
	
	Acceptor(String[] hostnames) throws IOException {
		this.hostnames = hostnames;	
		this.learners = new LearnerInterface[hostnames.length];		
		init();
	}

	/*
	 * Function to read in the stored values.
	 */
	private void init() {
		BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader(filename));
			String value = bf.readLine();
			if (value == null || value.isEmpty()) {
				currentN = 0;
			} else {
				currentN = Long.parseLong(value);
			}
			
			bf.close();
		} catch (IOException e) {
			currentN = 0;
		}
	}

	/*
	 * Function to save out the current state ot the backup file.
	 */
	private void writeToDisk() {
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(filename));
			bf.write(Long.toString(currentN));
			bf.flush();		
			bf.close();
		} catch (IOException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * @see AcceptorInterface#prepare(Operation)
	 */
	@Override
	public synchronized Operation prepare(Operation oper) throws RemoteException, RejectedException {
		System.out.println("Acceptor: Recieved prepare for "+ oper.operation + oper.key + oper.value + " with token " + oper.token + " and my token is " + currentN);
		
		// gremlins to cause random failures 
		Gremlin.Lookout lookout = new Gremlin.Lookout();
		Gremlin.GremlinStart(Thread.currentThread(), lookout, "Acceptor");
		
		// only accept token which is higher than what we have
		if (oper.token <= currentN)
			throw new RejectedException(currentN, RejectedException.RejectedReason.LowToken);
		
		// otherwise send the commitment and the current promised value if we have any.
		currentN = oper.token; // commit to not accepting anything with smaller token
		Operation operRet = lastOper; // last promised value
		lastOper = oper;
		
		// record the value on disk
		writeToDisk();
		
		// turn off the gremlins
		Gremlin.GremlinEnd(lookout);
		return operRet;
	}

	/*
	 * (non-Javadoc)
	 * @see AcceptorInterface#accept(Operation)
	 */
	@Override
	public synchronized void accept(Operation oper) throws RejectedException {
		System.out.println("Acceptor: Recieved accept for "+ oper.operation + oper.key + oper.value + " with token " + oper.token + " and my token is " + currentN);
		
		// gremlins to cause random failures 
		Gremlin.Lookout lookout = new Gremlin.Lookout();
		Gremlin.GremlinStart(Thread.currentThread(), lookout, "Acceptor");
		
		// only accept requests with token that is higher than what we have
		if (oper.token < currentN)
			throw new RejectedException(currentN, RejectedException.RejectedReason.LowToken);
		
		// send the accepted value to all the learners
		for (int i = 0; i < hostnames.length; i++) {
			Registry registry;
			try {
				registry = LocateRegistry.getRegistry(hostnames[i]);
				learners[i] = (LearnerInterface) registry.lookup(LearnerInterface.nameLearner);
				learners[i].learn(oper);
			} catch (RemoteException | NotBoundException e) {
				//continue
			}
			}

		// erase value from disk
		lastOper = null;	
		writeToDisk();
		

		// turn off the gremlins
		Gremlin.GremlinEnd(lookout);
		return;
	}
}
