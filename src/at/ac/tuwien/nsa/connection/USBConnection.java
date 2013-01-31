package at.ac.tuwien.nsa.connection;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import org.apache.log4j.Logger;

import at.ac.tuwien.nsa.datastore.ResultStorage;
import at.ac.tuwien.nsa.measurement.QualityMeasurementThread;
import at.ac.tuwien.nsa.protocol.AttentionMessage;
import at.ac.tuwien.nsa.protocol.HashResponse;
import at.ac.tuwien.nsa.protocol.Message;
import at.ac.tuwien.nsa.protocol.Message.ActionIdentifiers;
import at.ac.tuwien.nsa.protocol.ProtocolException;
import at.ac.tuwien.nsa.protocol.ResetReceivedException;
import at.ac.tuwien.nsa.protocol.Result;

/**
 * Represents a USBConnection from Client to Reader.
 */
public class USBConnection implements Connection {

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
	 * If the connection is open, this holds the current InputStream.
	 */
	private InputStream input;

	/**
	 * If the connection is open, this holds the current OutputStream.
	 */
	private OutputStream out;

	/**
	 * Logger.
	 */
	private static final Logger LOG = Logger.getLogger(USBConnection.class);

	/**
	 * Creates a new USBConnection object from a CommPortIdentifier instance.
	 */
	public USBConnection(CommPortIdentifier portIdentifier) {
		this.portIdentifier = portIdentifier;
	}

	@Override
	public boolean isActive() {
		return (this.port != null) && probeConnection();
	}

	/**
	 * Attempts to open a serial port using the information in portIdentifier.
	 * 
	 * @throws PortInUseException
	 *             if the port is already locked/in use
	 * @throws UnsupportedCommOperationException
	 *             if the port does not support our configuration set
	 * @throws IOException
	 */
	public void connect() throws IOException {
		try {
			this.port = (SerialPort) portIdentifier.open(
					USBConnection.class.getName(), 2000);
			this.port.setSerialPortParams(115200, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			this.port.disableReceiveTimeout();
			this.port.enableReceiveThreshold(1);
			this.input = this.port.getInputStream();
			this.out = this.port.getOutputStream();
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
	 * This implements the communication protocol established between the Reader
	 * (mobile) and the Client.
	 * 
	 * @param portIdentifier
	 *            the port information to use for this transmission
	 * @param message
	 *            the actual message to be transmitted
	 * @return the response of the reader
	 * @throws IOException
	 *             in case of connection troubles
	 * @throws ProtocolException
	 *             if an invalid state was reached in the communication protocol
	 * @throws ResetReceivedException
	 *             if the reader sent a reset message
	 */
	public Result transmit(Message message) throws IOException,
			ProtocolException, ResetReceivedException {

		HashResponse responseOK;
		AttentionMessage attn;

		if (port == null)
			this.connect();

		if (message instanceof AttentionMessage) {

			try {
				this.port.enableReceiveTimeout(300);
			} catch (UnsupportedCommOperationException e) {
				this.port.disableReceiveTimeout();
				throw new IOException("Cannot use ReceiveTimeout on ComPort "
						+ portIdentifier.getName() + ", no probing possible.");
			}

			out.write(message.getBytes());
			LOG.debug("Sent: " + message.toString());

			responseOK = new HashResponse(input);
			this.port.disableReceiveTimeout();
			responseOK.verify(message);
			return null;
		}

		// 1. SEND-ATTN
		attn = new AttentionMessage((short) (message.getLength()));
		out.write(attn.getBytes());
		LOG.debug("Sent: " + attn.toString());

		// 2. READ-OK
		responseOK = new HashResponse(input);
		responseOK.verify(attn);

		// 3. SEND-CMD+PAYLOAD
		out.write(message.getBytes());
		LOG.debug("Sent: " + message.toString());

		// 4. READ-OK
		responseOK = new HashResponse(input);
		responseOK.verify(message);

		// 5. READ-ATTN
		attn = new AttentionMessage(input);

		// determine next read buffer size
		short resultLength = attn.getNextCommandLength();

		// 6. SEND-OK
		responseOK = new HashResponse(attn);
		out.write(responseOK.getBytes());
		LOG.debug("Sent: " + responseOK.toString());

		// 7. READ-RESULT+PAYLOAD
		Result result = new Result(input, resultLength);

		// 8. SEND-OK
		responseOK = new HashResponse(result);
		out.write(responseOK.getBytes());
		LOG.debug("Sent: " + responseOK.toString());

		// return result
		return result;
	}

	/**
	 * Sends Attention-Command to verify if we have the correct port.
	 * 
	 * @param portIdentifier
	 *            the port information to probe
	 * @return true if the challenge was answered successfully, otherwise false
	 */
	private boolean probeConnection() {
		try {
			this.transmit(AttentionMessage.EMPTY);
			return true;
		} catch (IOException | ProtocolException | ResetReceivedException e) {
			return false;
		}
	}

	@Override
	public void disconnect() {
		try {
			this.port.getInputStream().close();
			this.port.getOutputStream().close();
			this.port.close();
			this.port = null;
		} catch (IOException | NullPointerException e) {
			// eat ALL the exceptions
		}
	}

	@Override
	public void startMeasurement(boolean download, ResultStorage resultStorage,
			QualityMeasurementThread qualityMeasurementThread)
			throws IOException, ProtocolException, ResetReceivedException {

		HashResponse responseOK;
		AttentionMessage attn;

		Message message = null;
		if (download) {
			message = Message.START_EVAL_DOWN;
		} else {
			message = Message.START_EVAL_UP;
		}

		if (port == null)
			this.connect();

		// 1. SEND-ATTN
		attn = new AttentionMessage((short) (message.getLength()));
		out.write(attn.getBytes());
		LOG.debug("Sent: " + attn.toString());

		// 2. READ-OK
		responseOK = new HashResponse(input);
		responseOK.verify(attn);

		// 3. SEND-CMD+PAYLOAD
		out.write(message.getBytes());
		LOG.debug("Sent: " + message.toString());

		// 4. READ-OK
		responseOK = new HashResponse(input);
		responseOK.verify(message);

		Result result = null;

		long startTimeMS = 0;
		long endTimeMS = 0;
		Calendar.getInstance().getTimeInMillis();

		int c = 0;
		while (true) {
			result = new Result(input, (short) 4);			
			if (isOk(result)) {
				if(download){
					LOG.info("Download " + (++c) + "started.");
				} else {
					LOG.info("Upload " + (++c) + "started.");
				}
				startTimeMS = Calendar.getInstance().getTimeInMillis();
				resultStorage.addNewMeasurement(download);
				qualityMeasurementThread.addQualityMeasurementNotification();
			} else {
				break;
			}
			result = new Result(input, (short) 4);
			
			if (isOk(result)) {
				if(download){
					LOG.info("Download " + (++c) + "finished.");
				} else {
					LOG.info("Upload " + (++c) + "finished.");
				}
				endTimeMS = Calendar.getInstance().getTimeInMillis();
				resultStorage.addDurationToLastIncompleteMeasurement(endTimeMS
						- startTimeMS);
			} else {
				break;
			}
		}

		if (wasTerminated(result)) {
			if(download){
				LOG.info("Download measurement terminated correctly.");
			} else {
				LOG.info("Upload measurement terminated correctly.");
			}
			
		} else {
			throw new ProtocolException(
					"The measurement failed for some reason.");
		}

	}

	private boolean isOk(Result result) {
		return result.isPositive();
	}

	private boolean wasTerminated(Result result) {
		return result.getActionIdentifier() == ActionIdentifiers.EVAL_FINISHED;
	}

}
