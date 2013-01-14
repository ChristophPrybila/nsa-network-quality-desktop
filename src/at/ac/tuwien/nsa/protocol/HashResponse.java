package at.ac.tuwien.nsa.protocol;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents the answer of one party to the challenge of another party.
 * Each transmission of the other party must be answered by an appropriate HashResponse.
 */
public class HashResponse extends Message {

	/**
	 * Constructs a HashResponse from message.
	 * 
	 * @param message the message to construct the HashResponse from
	 */
	public HashResponse(Message message) {
		super(ActionIdentifiers.RESULT, new byte[] { message.getHash() });
	}

	/**
	 * Construct a HashResponse from an input stream.
	 * 
	 * @param in the input stream
	 * @throws IOException in case of networking or stream troubles
	 * @throws ProtocolException if an invalid state was reached in the communication
	 * @throws ResetReceivedException if the other party sent a reset command
	 */
	public HashResponse(InputStream in) throws IOException, ProtocolException, ResetReceivedException {
		super(in, (short) 4);
	}

	/**
	 * Verifies whether the given message validates with this HashResponse.
	 * 
	 * @param message the given message to validate
	 * @throws ProtocolException if the message does not validate
	 */
	public void verify(Message message) throws ProtocolException {
		if (message.getHash() != this.getPayload()[0]) {
			throw new ProtocolException("Hash values do not match!");
		}
	}

}
