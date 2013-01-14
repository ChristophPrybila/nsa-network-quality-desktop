package at.ac.tuwien.nsa.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class AttentionMessage extends Message {

	/**
	 * The AttentionMessage with nextCommandLength = 0 is used to
	 * probe the connection between Client and Reader.
	 */
	public static final AttentionMessage EMPTY = new AttentionMessage((short) 0);

	public AttentionMessage(short nextCommandLength) {
		super(ActionIdentifiers.ATTENTION, null);

		Random newRandom = new Random();
		newRandom.setSeed(System.currentTimeMillis());
		this.setPayload(new byte[] { getBytesFromShort(nextCommandLength)[0],
			getBytesFromShort(nextCommandLength)[1], (byte) (newRandom.nextInt() & 0xff) });
	}

	public AttentionMessage(InputStream in) throws IOException, ProtocolException,
		ResetReceivedException {
		super(in, (short) 4);
	}

	public short getNextCommandLength() {
		return (short) ((this.getPayload()[1] & 0xff) << 8 | this.getPayload()[0] & 0xff);
	}

	private static byte[] getBytesFromShort(short x) {
		byte[] bytesOfShort = new byte[2];
		bytesOfShort[0] = (byte) (x & 0xff);
		bytesOfShort[1] = (byte) ((x >> 8) & 0xff);
		return bytesOfShort;
	}

}
