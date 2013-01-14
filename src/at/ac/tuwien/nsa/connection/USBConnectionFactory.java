package at.ac.tuwien.nsa.connection;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

public class USBConnectionFactory {

	/**
	 * Logger.
	 */
	private static final Logger LOG = Logger.getLogger(USBConnectionFactory.class);

	/**
	 * Enumerates ports, probes them and tries to establish a connection.
	 * 
	 * @return a  *disconnected*, but verified USBConnection object
	 * @throws PortInUseException if the port is already in use
	 * @throws UnsupportedCommOperationException if the setup went wrong
	 * @throws IOException in case of communication troubles
	 */
	public static USBConnection getConnection() throws PortInUseException,
		UnsupportedCommOperationException, IOException {
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();

		LOG.debug("Starting to probe ports.");

		while (ports.hasMoreElements()) {
			CommPortIdentifier portId = ports.nextElement();

			LOG.info("Probing port: " + portId.getName());
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				LOG.debug("Opening serial port " + portId.getName() + " for probing.");
				USBConnection u = new USBConnection(portId);
				u.connect();
				if (u.isActive()) {
					LOG.info("Probe successful for port: " + portId.getName());
					u.disconnect();
					return u;
				}
				u.disconnect();
			}
		}

		// did not probe successfully on any port :(
		return null;
	}

}
