package at.ac.tuwien.nsa.connection;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

public class USBModemConnectionFactory {

	/**
	 * Logger.
	 */
	private static final Logger logger = Logger.getLogger(USBModemConnectionFactory.class);

	/**
	 * Enumerates ports, probes them and tries to establish a connection.
	 * 
	 * @return a USBConnection object
	 * @throws PortInUseException if the port is already in use
	 * @throws UnsupportedCommOperationException if the setup went wrong
	 * @throws IOException in case of communication troubles
	 */
	public static USBModemConnection getConnection() throws PortInUseException,
		UnsupportedCommOperationException, IOException {
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();

		logger.debug("Starting to probe ports for modem.");

		while (ports.hasMoreElements()) {
			CommPortIdentifier portId = ports.nextElement();

			logger.info("Probing port: " + portId.getName());
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				logger.debug("Opening serial port " + portId.getName() + " for probing.");
				try{
					USBModemConnection u = new USBModemConnection(portId);
					if (u.isActive()) {
						logger.info("Probe successful for port: " + portId.getName());
						return u;
					}
					u.disconnect();
				} catch(Exception e){
					//Ignore Exceptions
				}
			}
		}

		// did not probe successfully on any port :(
		return null;
	}

}
