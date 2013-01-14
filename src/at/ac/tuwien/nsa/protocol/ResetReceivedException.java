package at.ac.tuwien.nsa.protocol;

/**
 * A ResetReceivedException is thrown whenever one party receives a reset command from the other
 * party.
 */
public class ResetReceivedException extends Exception {

	private static final long serialVersionUID = 1L;

	public ResetReceivedException(String message) {
		super(message);
	}

}
