import java.io.Serializable;

/*
 * This class stores the operation that is being proposed.
 */
public class Operation implements Serializable{
	private static final long serialVersionUID = 10L;
	OperationVerbs operation; 
	String key;
	String value;
	
	long token;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Operation other = (Operation) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (operation != other.operation)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
