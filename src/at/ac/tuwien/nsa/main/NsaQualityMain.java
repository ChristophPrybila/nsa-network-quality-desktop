package at.ac.tuwien.nsa.main;

import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import at.ac.tuwien.nsa.connection.Connection;
import at.ac.tuwien.nsa.connection.USBConnectionFactory;
import at.ac.tuwien.nsa.protocol.Message;
import at.ac.tuwien.nsa.protocol.ProtocolException;
import at.ac.tuwien.nsa.protocol.ResetReceivedException;

public class NsaQualityMain {
	
	private static final Logger LOG = Logger.getLogger(Message.class);

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Connection c = null;
		try {
			c = USBConnectionFactory.getConnection();
			c.connect();
		} catch (PortInUseException | UnsupportedCommOperationException
				| IOException e) {
			LOG.info("could not find USB Device");
			e.printStackTrace();
			return;
		}
		
		//do Measurement here
		LOG.info("start download");
		try {
			c.startMeasurement(true);
		} catch (IOException | ProtocolException | ResetReceivedException e) {
			LOG.info("Error during measurement");
			e.printStackTrace();
		}
		c.disconnect();
	}

}
