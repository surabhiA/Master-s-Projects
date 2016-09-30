import java.io.Serializable;

/**
 * This class is used to format data which can be used to serialize the request to be sent by the client to the server
 * It also defines getter and setter for each member variable 
 * @author Surabhi Agrawal
 */


public class KVRequest implements Serializable {

	private static final long serialVersionUID = -4161900032727363951L;
	String instruction;
	String key;
	String value;
	
	public KVRequest(String instruction,String key,String value){
		setInstruction(instruction);
		setKey(key);
		setValue(value);
	}
	
	public KVRequest(String instruction,String key){
		setInstruction(instruction);
		setKey(key);
	}
	
	public String getInstruction() {
		return instruction;
	}
	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
