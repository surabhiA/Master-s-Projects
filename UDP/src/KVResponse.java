import java.io.Serializable;

/**
 * This class is used to format data which can be used to serialize the response to be sent by the server to the client
 * It also defines getter and setter for each member variable
 * @author Surabhi Agrawal
 */

public class KVResponse implements Serializable {
	
	private static final long serialVersionUID = -731871153538849965L;
	String value;         	//value retrieved from the KVStore
	String requestedKey;	//key requested by client to check if the response received at client has a valid key
	String status;			//Status of the transaction. Successful/Unsuccessful
	
	
	public KVResponse(String requestedKey, String value,String status){
		setRequestedKey(requestedKey);
		setValue(value);
		setStatus(status);
	}
	
	public KVResponse(String requestedKey, String status){
		setRequestedKey(requestedKey);
		setStatus(status);
	}
	
	public String getValue() {
		return value;
	}
	
	public String getRequestedKey() {
		return requestedKey;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void setRequestedKey(String requestedKey) {
		this.requestedKey = requestedKey;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		if(value != null){
			return status + " Value of key " + requestedKey + " is " + value;   //Displays message for a successful get operation
		}
		else{
			return status; //Displays message for all other scenarios
		}
	}
	
}
