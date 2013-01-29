package at.ac.tuwien.nsa.main;

import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import at.ac.tuwien.nsa.connection.Connection;
import at.ac.tuwien.nsa.connection.ModemConnection;
import at.ac.tuwien.nsa.connection.USBConnectionFactory;
import at.ac.tuwien.nsa.connection.USBModemConnectionFactory;
import at.ac.tuwien.nsa.datastore.MeasurementPlace;
import at.ac.tuwien.nsa.datastore.ResultStorage;
import at.ac.tuwien.nsa.measurement.ConnectionMeasurementThread;
import at.ac.tuwien.nsa.measurement.QualityMeasurementThread;

public class NsaQualityMain {

	private static final Logger LOG = Logger.getLogger(NsaQualityMain.class);
	private static final MeasurementPlace place = new MeasurementPlace(
			"Technical University Vienna EI", 48.19606, 16.369947);

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Connection c = null;
		ModemConnection cModem = null;
		try {
			cModem = USBModemConnectionFactory.getConnection();
			if(cModem == null) {
				throw new IOException("No Modem found.");
			}
		} catch (PortInUseException | UnsupportedCommOperationException
				| IOException e) {
			LOG.error("could not find USB Modem", e);
			return;
		}
		try {
			c = USBConnectionFactory.getConnection();
			if(c == null) {
				throw new IOException("No Device found.");
			}
			c.connect();
		} catch (PortInUseException | UnsupportedCommOperationException
				| IOException e) {
			LOG.error("could not find USB Device", e);
			return;
		}
		
		LOG.info("init ResultStorage");
		ResultStorage resultStorage = new ResultStorage(place);
		QualityMeasurementThread qualityMeasurementThread = new QualityMeasurementThread(
				resultStorage, cModem);
		LOG.info("starting QualityMeasurementThread");
		qualityMeasurementThread.start();
		ConnectionMeasurementThread connectionMeasurement = new ConnectionMeasurementThread(
				c, resultStorage, qualityMeasurementThread);
		LOG.info("starting ConnectionMeasurementThread");
		connectionMeasurement.start();
	}

}
