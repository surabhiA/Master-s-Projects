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
 * This class implements the Proposer role of the paxos protocol
 */
public class Proposer implements ProposerInterface {
	private int uniqueID; // the unique ID used to make the unique proposal numbers
	private long currentN; // current token number
	private final String filename = "proposer.bak"; // backup file to store the token number
	
	private String[] hostnames; // list of servers
	private AcceptorInterface[] acceptors; 
	
	Proposer(int uniqueID, String[] hostnames) {
		this.uniqueID = uniqueID;
		this.hostnames = hostnames;
		this.acceptors = new AcceptorInterface[hostnames.length];
	}

	/*
	 * Function to read in the stored token number.
	 */
	private void init() {
		BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader(filename));
			String value = bf.readLine();
			if (value == null || value.isEmpty()) {
				currentN = uniqueID;
			} else {
				currentN = Long.parseLong(value);
			}
			bf.close();
		} catch (IOException e) {
			currentN = 0;
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see ProposerInterface#driveConcensus(Operation)
	 */
	@Override
	public synchronized void driveConcensus(Operation oper) throws IOException, TimedOutException, RemoteException {		
		int retries = 0; // max number of rounds of the paxos protocol. This is used to prevent a live lock.
		boolean fRetry = true;
		boolean fSentOper = false;
		
		init();

		// gremlins to cause random failures 
		Gremlin.Lookout lookout = new Gremlin.Lookout();
		Gremlin.GremlinStart(Thread.currentThread(), lookout, "Proposer");
		
		while((fRetry || !fSentOper) && retries < 20) {
			Operation operMax = null;
			
			// generate N
			generateN();
			oper.token = currentN;
						
			// send the propose to the Acceptors
			int countAccepted = 0; // count of acceptors who accepted our proposal	
			for (int i = 0; i < hostnames.length; i++) {
				Registry registry;
				try {
					registry = LocateRegistry.getRegistry(hostnames[i]);
					acceptors[i] = (AcceptorInterface) registry.lookup(AcceptorInterface.nameAcceptor);
					Operation operRet = acceptors[i].prepare(oper);
					if (operRet != null && (operMax == null || operRet.token > operMax.token)) {
						operMax = operRet;
					}
					countAccepted++;					
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
					//continue
				} catch (RejectedException e) {
					currentN = currentN > e.getToken() ? currentN : e.getToken();
				}
				} 

			System.out.println("Proposer: Prepare " + oper.operation + oper.key + oper.value + " with token " + oper.token);
			System.out.println("Proposer: Number of acceptors accepting the proposal = " + countAccepted);
			
			if (countAccepted <= hostnames.length/2) {
				// less than half of acceptors accepted. Retry.
				fRetry = true;
				retries++;
				continue;
			} else {
				fRetry = false;				
			}
						
			// send accept to Acceptors
			if (operMax == null) {
				operMax = oper;
				fSentOper = true;
			} else {
				// if acceptors sent a operation, send that
				operMax.token = oper.token;
			}

			int countAccept = 0; // count of acceptors who accepted our accpet	
			for (int i = 0; i < hostnames.length; i++) {
				Registry registry;
				try {
					registry = LocateRegistry.getRegistry(hostnames[i]);
					acceptors[i] = (AcceptorInterface) registry.lookup(AcceptorInterface.nameAcceptor);
					acceptors[i].accept(operMax);
					countAccept++;
				} catch (RemoteException | NotBoundException e) {
					//continue
				} catch (RejectedException e) {
					currentN = currentN > e.getToken() ? currentN : e.getToken();
				}
				}

			System.out.println("Proposer: Accept " + oper.operation + oper.key + oper.value + " with token " + oper.token);
			System.out.println("Proposer: Number of acceptors accepting the proposal = " + countAccept);

			if (countAccept <= hostnames.length/2) {
				fRetry = true;
				fSentOper = false;
			}
			
			// store the N	in the backup file
			BufferedWriter bf = new BufferedWriter(new FileWriter(filename));
			bf.write(Long.toString(currentN));
			bf.flush();			
			bf.close();
		}
		
		// we weren't able to send in our proposal. Throw the TimedOut exception.
		if (fSentOper == false) {
			throw new TimedOutException();
		}

		// turn off the gremlins
		Gremlin.GremlinEnd(lookout);
	}

	/*
	 * Function to generate unique token numbers in sequential order
	 */
	private void generateN() {
		currentN = (currentN/10) * 10 + 10 + uniqueID;		
	}
}
