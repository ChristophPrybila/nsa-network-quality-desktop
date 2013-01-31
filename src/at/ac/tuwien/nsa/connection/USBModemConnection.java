package at.ac.tuwien.nsa.connection;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Represents a USBConnection from SMSClient to Modem.
 */
public class USBModemConnection implements ModemConnection {

	private static final int DEFAULT_RECEIVE_THRESHOLD = 6;
	//message Border \r\nRESULT\r\n\r\nOK\r\n
	private static String msb = "\r\n";

	/**
	 * Holds the port information used to establish a connection in the event of
	 * a transmission. This is initially null, the constructor then attempts to
	 * determine the correct information by probing all available serial ports.
	 */
	private CommPortIdentifier portIdentifier;

	/**
	 * If the connection is open, this holds the current open serial port.
	 */
	private SerialPort port;

	/**
	 * Logger.
	 */
	private static final Logger logger = Logger.getLogger(USBModemConnection.class);

	/**
	 * Creates a new USBConnection object from a CommPortIdentifier instance.
	 */
	public USBModemConnection(CommPortIdentifier portIdentifier) throws IOException {
		this.portIdentifier = portIdentifier;
		connect();
	}

	@Override
	public boolean isActive() {
		return (this.port != null) && probeConnection();
	}

	/**
	 * Attempts to open a serial port using the information in portIdentifier.
	 * 
	 * @throws PortInUseException
	 *         if the port is already locked/in use
	 * @throws UnsupportedCommOperationException
	 *         if the port does not support our configuration set
	 */
	private void connect() throws IOException {
		try {
			this.port = (SerialPort) portIdentifier.open(USBModemConnection.class.getName(), 2000);
			this.port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
			this.port.enableReceiveTimeout(1000);
			this.port.enableReceiveThreshold(DEFAULT_RECEIVE_THRESHOLD);
			this.port.getOutputStream().flush();
		} catch (UnsupportedCommOperationException | PortInUseException e) {
			// if it didn't work out, close the port again and reset
			if (this.port != null) {
				this.port.close();
				this.port = null;
			}
			throw new IOException(e);
		}
	}

	/**
	 * Sends ATcommand to verify if we have the correct port.
	 * 
	 * @return true if the challenge was answered successfully, otherwise false
	 */
	private boolean probeConnection() {
		final String probeModemMsg = "ATE0";
		final String isModemResult = "OK";
		final String probeManufacturer = "AT+CGMM";
		final String isNokiaResult = "Nokia 6212 classic"+msb+msb+"OK";
		boolean isModem = false;
		boolean isNokia = false;

		String resultString = null;
		try {
			resultString = transmit(probeModemMsg, isModemResult.length());
			resultString = transmit(probeModemMsg, isModemResult.length());
			isModem = resultString != null ? isModemResult.equals(resultString) : false;
			if (isModem) {
				resultString = transmit(probeManufacturer, isNokiaResult.length());
				isNokia = resultString != null ? isNokiaResult.equals(resultString) : false;
			}
		} catch (IOException e) {
			resultString = null;
		}
		return isModem && isNokia;
	}
	
	@Override
	public String transmit(String atCommand, int lengthOfResult) throws IOException {
		//logger.debug("Sending: " + atCommand.trim());
		
		if(!atCommand.endsWith("\r\n") && !atCommand.endsWith("" + '\032')) {
			atCommand += "\r\n";
		}
		
		this.port.getOutputStream().write((atCommand).getBytes());
		String response = new String(readFromStream(lengthOfResult)).trim();
		//logger.debug("Received: " + response);
		return response;
	}
	
	private byte[] readFromStream(int length) throws IOException {
		byte[] responseBuffer = new byte[length + 4];
		try {
			this.port.enableReceiveThreshold(length + 4);
			this.port.getInputStream().read(responseBuffer);
			this.port.enableReceiveThreshold(DEFAULT_RECEIVE_THRESHOLD);
		} catch (UnsupportedCommOperationException e) {
			throw new IOException("Could not set port options.");
		}
		return responseBuffer;
	}

	@Override
	public void disconnect() {
		if (port != null) {
			port.close();
		}
		port = null;
	}
	
	protected static String bytesToHex(byte[] bytes) {
		final char[] hexArray = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
		};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	@Override
	public String readGarbage(int lengthOfGarbage) throws IOException {
		byte[] responseBuffer = new byte[lengthOfGarbage];
		try {
			this.port.enableReceiveThreshold(lengthOfGarbage);
			this.port.getInputStream().read(responseBuffer);
			this.port.enableReceiveThreshold(DEFAULT_RECEIVE_THRESHOLD);
		} catch (UnsupportedCommOperationException e) {
			throw new IOException("Could not set port options.");
		}
		return new String(responseBuffer);
	}

}
