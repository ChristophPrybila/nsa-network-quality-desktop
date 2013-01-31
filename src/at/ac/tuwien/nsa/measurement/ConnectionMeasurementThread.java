package at.ac.tuwien.nsa.measurement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import at.ac.tuwien.nsa.connection.Connection;
import at.ac.tuwien.nsa.datastore.ResultStorage;
import at.ac.tuwien.nsa.protocol.ProtocolException;
import at.ac.tuwien.nsa.protocol.ResetReceivedException;

public class ConnectionMeasurementThread extends Thread {

	private static final Logger LOG = Logger
			.getLogger(ConnectionMeasurementThread.class);
	private Connection c = null;
	private ResultStorage resultStorage;
	private QualityMeasurementThread qualityMeasurementThread;

	public ConnectionMeasurementThread(Connection c,
			ResultStorage resultStorage,
			QualityMeasurementThread qualityMeasurementThread) {
		super();
		this.c = c;
		this.resultStorage = resultStorage;
		this.qualityMeasurementThread = qualityMeasurementThread;
	}

	@Override
	public void run() {
		LOG.info("starting download measurement");
		try {
			c.startMeasurement(true, resultStorage, qualityMeasurementThread);
		} catch (IOException | ProtocolException | ResetReceivedException e) {
			LOG.info("Error during measurement");
			e.printStackTrace();
		}
		
		LOG.info("starting upload measurement");
		try {
			c.startMeasurement(false, resultStorage, qualityMeasurementThread);
		} catch (IOException | ProtocolException | ResetReceivedException e) {
			LOG.info("Error during measurement");
			e.printStackTrace();
		}
		stopThread();
		LOG.info("ConnectionMeasurementThread stopped");
	}

	public void stopThread() {
		if (c != null) {
			c.disconnect();
		}
		if (qualityMeasurementThread != null) {
			qualityMeasurementThread.stopThread();
		}
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File("result.json"));
			fw.write(resultStorage.toJSON());
			fw.close();
		} catch (IOException e) {
			System.err.println("Could not write to file, printing to standard output instead.");
			System.out.println(resultStorage.toJSON());
		}
	}

}
