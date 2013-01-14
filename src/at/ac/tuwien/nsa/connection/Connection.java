package at.ac.tuwien.nsa.connection;

import java.io.IOException;
import java.util.List;

import at.ac.tuwien.nsa.protocol.Message;
import at.ac.tuwien.nsa.protocol.ProtocolException;
import at.ac.tuwien.nsa.protocol.ResetReceivedException;
import at.ac.tuwien.nsa.protocol.Result;

/**
 * Defines the basic methods required to communicate with a Reader.
 */
public interface Connection {
	
	/**
	 * Opens the connection.
	 * 
	 * @throws IOException if anything goes wrong
	 */
	void connect() throws IOException;
	
	/**
	 * Closes the connection.
	 */
	void disconnect();

	/**
	 * @return true if connection is active, false if not.
	 */
	boolean isActive();

	/**
	 * transmit a command and receive a response
	 * 
	 * @param message the message to send
	 * @return response the result of the command
	 * @throws ProtocolException if an invalid state was reached in the communication
	 * @throws ResetReceivedException if the other party sent a reset command
	 */
	Result transmit(Message message) throws IOException, ProtocolException, ResetReceivedException;

	/**
	 * Starts a Measurement as download or upload and returns a list
	 * of ms entries, determining the needed time per iteration.
	 */
	List<Long> startMeasurement(boolean download) throws IOException, ProtocolException, ResetReceivedException;
	
}
