package at.ac.tuwien.nsa.protocol;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a result from an operation that was performed by one party.
 */
public class Result extends Message {
	
	/**
	 * Constructs a Result message with a given payload
	 * @param payload result payload
	 */
	public Result(byte[] payload) {
		super(ActionIdentifiers.RESULT, payload);
	}
	
	/**
	 * Constructs a Result object from an input stream with the given length.
	 * 
	 * @param stream input stream to read from
	 * @param length length to read
	 * @throws IOException in case of transmission or stream troubles
	 * @throws ProtocolException if an invalid state in the communication was reached
	 * @throws ResetReceivedException if a reset command was received from the stream
	 */
	public Result(InputStream stream, short length) throws IOException, ProtocolException, ResetReceivedException {
		super(stream, length);
		if (this.getActionIdentifier() != ActionIdentifiers.RESULT && this.getActionIdentifier() != ActionIdentifiers.EVAL_FINISHED)
			throw new ProtocolException("Not a result.");
	}
	
	/**
	 * Constructs a simple boolean Result message
	 * @param bool positive result if true, else negative
	 */
	public Result(boolean bool) {
		super(ActionIdentifiers.RESULT, new byte[] {
			(bool) ? ActionIdentifiers.POSITIVE : ActionIdentifiers.NEGATIVE
		});
	}
	
	/**
	 * @return true if this is a simple positive result
	 */
	public boolean isPositive() {
		return this.getPayload()[0] == ActionIdentifiers.POSITIVE;
	}

}
