package at.ac.tuwien.nsa.connection;

import java.io.IOException;

/**
 * Defines the basic methods required to communicate with a USB Modem.
 */
public interface ModemConnection {

	/**
	 * @return true if connection is active, false if not.
	 */
	boolean isActive();
	
	/**
	 * transmit an AT-command
	 * 
	 * @param msisdn number to send the message to
	 * @param message the message to send
	 * @return Returns the response of the modem
	 */
	String transmit(String atCommand, int lengthOfResult) throws IOException;	
	
	/**
	 * Closes the port of the connection.
	 */
	void disconnect();
	
	/**
	 * Reads 'lengthOfGarbage' bytes from the inputstream
	 */
	String readGarbage(int lengthOfGarbage) throws IOException;	

}
