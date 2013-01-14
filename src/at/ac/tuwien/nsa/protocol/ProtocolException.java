package at.ac.tuwien.nsa.protocol;

/**
 * A ProtocolException is thrown whenever the communication between Client and Reader reaches an
 * invalid or undefined state.
 */
public class ProtocolException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProtocolException(String message) {
		super(message);
	}

}
