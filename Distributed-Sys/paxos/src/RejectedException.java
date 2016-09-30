/*
 * Exception to denote why a proposal or accept message is rejected by the acceptor
 */
public class RejectedException extends Exception {
	private static final long serialVersionUID = 1L;

	enum RejectedReason {
		LowToken,
	}
	
	private long token;
	private RejectedReason reason;

	public long getToken() {
		return token;
	}
	public RejectedReason getReason() {
		return reason;
	}
	
	public RejectedException(long token, RejectedReason reason) {
		super();
		this.token = token;
		this.reason = reason;
	}	
}
