package at.ac.tuwien.nsa.protocol;

import java.io.IOException;
import java.io.InputStream;

public class Message {

	public final class ActionIdentifiers {

		/* Method identifiers. */
		public static final byte ATTENTION = 0x2a;
		public static final byte RESULT = 0x5d;
		public static final byte IS_CARD_PRESENT = 0x3b;
		public static final byte CLOSE = 0x01;
		public static final byte GET_ATR = 0x02;
		public static final byte GET_CHANNEL_NUMBER = 0x03;
		public static final byte TRANSMIT = 0x04;
		
		public static final byte START_EVAL_UP = 0x07;
		public static final byte START_EVAL_DOWN = 0x08;
		public static final byte EVAL_FINISHED = 0x09;

		/* Generic response codes for boolean methods. */
		public static final byte POSITIVE = 0x01;
		public static final byte NEGATIVE = 0x00;
	}

	public static final Message START_EVAL_UP = new Message(ActionIdentifiers.START_EVAL_UP, null);
	public static final Message START_EVAL_DOWN = new Message(ActionIdentifiers.START_EVAL_DOWN, null);
	public static final Message EVAL_FINISHED = new Message(ActionIdentifiers.EVAL_FINISHED, null);
	
	public static final Message IS_CARD_PRESENT = new Message(ActionIdentifiers.IS_CARD_PRESENT, null);
	public static final Message CLOSE = new Message(ActionIdentifiers.CLOSE, null);
	public static final Message GET_ATR = new Message(ActionIdentifiers.GET_ATR, null);
	public static final Message GET_CHANNEL_NUMBER = new Message(
		ActionIdentifiers.GET_CHANNEL_NUMBER, null);
	public static final Message RESET = new Message((byte) 0xff, new byte[] {
		(byte) 0xff, (byte) 0xff, (byte) 0xff
	});

	private byte actionIdentifier;
	private byte[] payload;

	/**
	 * There is no argumentless Constructor.
	 */
	private Message() {}

	/**
	 * Construct a new message from an input stream.
	 * 
	 * @param inputMessage
	 *        the incoming bytes
	 * @return a message object
	 * @throws IOException
	 * @throws ResetReceivedException
	 */
	public Message(InputStream stream, short length) throws IOException, ProtocolException,
		ResetReceivedException {
		if (length < 4)
			throw new ProtocolException("Must read at least 4 bytes.");

		byte[] inputMessage = new byte[length];

		for (int i = 0; i < length; i++) {
			inputMessage[i] = (byte) stream.read();
			if (i == 3) {
				boolean isReset = true;
				for (short j = 0; j < 4; j++) {
					if (inputMessage[j] != (byte) 0xff) {
						isReset = false;
						break;
					}
				}
				if (isReset) {
					throw new ResetReceivedException("Reset occurred.");
				}
			}
		}

		/* extract the payload */
		byte[] payload = new byte[inputMessage.length - 1];
		for (int i = 1; i < inputMessage.length; i++) {
			payload[i - 1] = inputMessage[i];
		}

		this.actionIdentifier = inputMessage[0];
		this.payload = payload;
	}

	/**
	 * Construct a new message from the parameters.
	 * 
	 * @param actionIdentifier
	 *        identifies the desired action
	 * @param payload
	 *        the payload bytes
	 */
	public Message(byte actionIdentifier, byte[] payload) {
		this();
		this.actionIdentifier = actionIdentifier;
		this.payload = (payload == null) ? new byte[] {} : payload;
	}

	/**
	 * retrieve the payload bytes from the message
	 * 
	 * @return the payload bytes
	 */
	public byte[] getPayload() {
		return this.payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	/**
	 * @return the complete message bytes
	 */
	public byte[] getBytes() {
		byte[] buffer = new byte[this.getPayload().length + 1];
		buffer[0] = this.getActionIdentifier();
		for (short i = 0; i < this.getPayloadLength(); i++) {
			buffer[i + 1] = this.getPayload()[i];
		}

		return Message.addPadding(buffer);
	}

	/**
	 * @return the payload length
	 */
	private int getPayloadLength() {
		return this.getPayload().length;
	}

	/**
	 * retrieve the length of complete message
	 * 
	 * @return the payload length
	 */
	public int getLength() {
		return this.getBytes().length;
	}

	/**
	 * retrieve the action identifier
	 * 
	 * @return the action identifier
	 */
	public byte getActionIdentifier() {
		return this.actionIdentifier;
	}

	/**
	 * Calculate the hash value of this message.
	 * 
	 * @return hash value
	 */
	public byte getHash() {
		byte total = 0;
		for (int i = 0; i < this.getLength(); i++) {
			total += (byte) this.getBytes()[i];
		}
		return total;
	}

	/**
	 * Return a string representation of the Message.
	 */
	public String toString() {
		int index = this.getClass().getName().lastIndexOf('.') + 1;
		String cs = this.getClass().getName().substring(index);
		return cs + "(actionIdentifier = 0x" + bytesToHex(new byte[] {
			this.getActionIdentifier()
		}) + ", payload = " + bytesToHex(this.getPayload()) + ") = " + bytesToHex(this.getBytes())
			+ " >> hash = 0x" + bytesToHex(new byte[] {
				this.getHash()
			});
	}

	/**
	 * Ensures that a message is always at least four bytes long.
	 * 
	 * @param bytes
	 *        the original message
	 * @return the padded message
	 */
	public static byte[] addPadding(byte[] bytes) {
		if (bytes.length >= 4)
			return bytes;

		byte[] buffer = new byte[] {
			0x00, 0x00, 0x00, 0x00
		};

		for (int i = 0; i < bytes.length; i++) {
			buffer[i] = bytes[i];
		}

		return buffer;
	}

	/**
	 * Helper method to display a byte[] as String.
	 * 
	 * @param bytes
	 *        input byte[]
	 * @return a string representation of the input byte[]
	 */
	public static String bytesToHex(byte[] bytes) {
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

}
