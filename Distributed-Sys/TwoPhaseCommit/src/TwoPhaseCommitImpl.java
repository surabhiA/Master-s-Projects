import java.rmi.RemoteException;

/*
 * This class implements the two phase commit protocol.
 * This supports rollback of the commit values
	 * Vote message clears the stored values.
	 * Commit message stores the last values
	 * Abort message applies back the stored values, if present.
 */
public class TwoPhaseCommitImpl implements TwoPhaseCommit{

	private DataStore data;
	
	public TwoPhaseCommitImpl(DataStore data) {
		this.data = data;
	}

	@Override
	public ResponseTPC vote(String instruction, String key, String value) throws RemoteException {
		data.startTwoPhaseCommit();
		if (instruction.equals("put"))
			return ResponseTPC.ACK;
		if(data.containsKey(key)) {
			return ResponseTPC.ACK;
		}
		data.endTwoPhaseCommit();
		return ResponseTPC.NotReady;
	}

	@Override
	public void commit(String instruction,String key,String value) throws RemoteException, RollbackException {
		if(instruction.equals("put")){
			data.put(key,value);
		}
		else{
			if(data.containsKey(key)){
				data.remove(key);
			} else {
				throw new RollbackException();
			}			
		}
		data.endTwoPhaseCommit();
	}
	
	@Override
	public void abort() throws RemoteException {
		data.endTwoPhaseCommit();
	}
	
	
}
