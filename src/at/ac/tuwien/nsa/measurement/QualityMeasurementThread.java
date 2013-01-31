package at.ac.tuwien.nsa.measurement;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import at.ac.tuwien.nsa.connection.ModemConnection;
import at.ac.tuwien.nsa.datastore.QualityData;
import at.ac.tuwien.nsa.datastore.ResultStorage;

public class QualityMeasurementThread extends Thread {

	private static final Logger LOG = Logger
			.getLogger(QualityMeasurementThread.class);
	private BlockingQueue<Byte> notificationQueue;
	private boolean isRunning;
	private ResultStorage resultStorage;
	private ModemConnection c = null;

	// message Border \r\nRESULT\r\n\r\nOK\r\n
	private static String msb = "\r\n";

	public QualityMeasurementThread(ResultStorage resultStorage,
			ModemConnection c) {
		super();
		this.notificationQueue = new LinkedBlockingQueue<Byte>();
		this.isRunning = true;
		this.resultStorage = resultStorage;
		this.c = c;
	}

	public void addQualityMeasurementNotification() {
		try {
			notificationQueue.put(new Byte((byte) 0x42));
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				notificationQueue.take();
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
			}
			if (isRunning) {
				resultStorage
						.addQualityToLastIncompleteMeasurement(measureQuality());
			} else {
				break;
			}
		}
		LOG.info("QualityMeasurementThread stopped");
	}

	public void stopThread() {
		isRunning = false;
		c.disconnect();
		addQualityMeasurementNotification();
	}

	private QualityData measureQuality() {

		// AT+COPS? => +COPS: 0,2,"23210" + OK
		String connectionOptions = "AT+COPS?";
		int connectionOptionsResultLength = ("+COPS: 0,2,'23210'" + msb + msb + "OK")
				.length();

		// AT+CREG=2 => OK
		String connectionRegistrationActivation = "AT+CREG=2";
		int connectionRegistrationActivationResultLength = ("OK").length();

		// AT+CREG? => +CREG: 2,1,"07DA","BF74" + OK
		String connectionRegistration = "AT+CREG?";
		int connectionRegistrationResultLength = ("+CREG: 2,1,'07DA','BF74'"
				+ msb + msb + "OK").length();
		
		//AT+CSQ => +CSQ: 12,99 WARNING digits can be smaller 10
		String connectionQuality = "AT+CSQ";
		int connectionQualityResultLength = ("+CSQ: 12,99"
				+ msb + msb).length();
		
		int lac = -1;
		int cellid = -1;
		int mcc = -1;
		int mnc = -1;
		int csq = -1;
		
		try {
			String countryAndNation = c.transmit(connectionOptions,
					connectionOptionsResultLength);	
			//+COPS: 0,2,"23210" + OK (232 = mcc, 10 = mnc) 
	        String countryAndNationRegEx = "\\+COPS: .+?,\"([0-9]+)\"*";
	        Pattern countryAndNationPattern = Pattern.compile(countryAndNationRegEx);
	        Matcher countryAndNationMatcher =  countryAndNationPattern.matcher(countryAndNation);
	        countryAndNationMatcher.find();
	        try {
		        mcc = Integer.parseInt(countryAndNationMatcher.group(1).substring(0,3));
		        mnc = Integer.parseInt(countryAndNationMatcher.group(1).substring(3));
	        } catch (Exception e) {
	        	System.out.println("COPS: " + countryAndNation);
	        }

			//Read OK
			c.transmit(connectionRegistrationActivation,
					connectionRegistrationActivationResultLength);

			String locationAreaAndCellId = c.transmit(connectionRegistration,
					connectionRegistrationResultLength);
			// +CREG: 2,1,"07DA","BF74" + OK (07DA = lac, BF74 = cellid) 
	        String locationAreaAndCellIdRegEx = "\\+CREG: .+?,\"([0-9a-fA-F]+)\",\"([0-9a-fA-F]+)\"*";
	        Pattern locationAreaAndCellIdPattern = Pattern.compile(locationAreaAndCellIdRegEx);
	        Matcher locationAreaAndCellIdMatcher =  locationAreaAndCellIdPattern.matcher(locationAreaAndCellId);
	        locationAreaAndCellIdMatcher.find();
			lac = Integer.parseInt(locationAreaAndCellIdMatcher.group(1),16);
			cellid = Integer.parseInt(locationAreaAndCellIdMatcher.group(2),16);

			String signalQuality = c.transmit(connectionQuality,
					connectionQualityResultLength);
			//AT+CSQ => +CSQ: 12,99 (12 = csq) 
	        String signalQualityRegEx = "\\+CSQ: ([0-9]+),*";
	        Pattern signalQualityPattern = Pattern.compile(signalQualityRegEx);
	        Matcher signalQualityMatcher =  signalQualityPattern.matcher(signalQuality);
	        signalQualityMatcher.find();
	        csq = Integer.parseInt(signalQualityMatcher.group(1));
	        
			//read missing bytes
			if(!signalQuality.endsWith("OK")) {
				if(signalQuality.endsWith("O")) {
					c.readGarbage(1);
				} else {
					c.readGarbage(2);
				}
			} 
			
		} catch (IOException e) {
			LOG.error("Error during quality measurement at modem.",e);
		}
		return new QualityData(lac, cellid, mcc, mnc, csq);
	}
}
