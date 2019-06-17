package com.coap.elements.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class describes the functionality to write raw network-ordered datagrams
 * on bit-level.
 */
public final class DatagramWriter {

	// Attributes //////////////////////////////////////////////////////////////

	private final ByteArrayOutputStream byteStream;

	private byte currentByte;
	private int currentBitIndex;

	// Constructors ////////////////////////////////////////////////////////////

	/**
	 * Creates a new empty writer.
	 */
	public DatagramWriter() {

		// initialize underlying byte stream
		byteStream = new ByteArrayOutputStream();

		// initialize bit buffer
		currentByte = 0;
		currentBitIndex = Byte.SIZE - 1;
	}

	// Methods /////////////////////////////////////////////////////////////////

	/**
	 * Writes a sequence of bits to the stream.
	 * 
	 * @param data
	 *            A Long containing the bits to write.
	 * 
	 * @param numBits
	 *            The number of bits to write.
	 */
	public void writeLong(final long data, final int numBits) {

		if (numBits < 32 && data >= (1 << numBits)) {
			throw new IllegalArgumentException(String.format("Truncating value %d to %d-bit integer", data, numBits));
		}

		for (int i = numBits - 1; i >= 0; i--) {

			// test bit
			boolean bit = (data >> i & 1) != 0;
			if (bit) {
				// set bit in current byte
				currentByte |= (1 << currentBitIndex);
			}

			// decrease current bit index
			--currentBitIndex;

			// check if current byte can be written
			if (currentBitIndex < 0) {
				writeCurrentByte();
			}
		}
	}

	/**
	 * Writes a sequence of bits to the stream.
	 * 
	 * @param data
	 *            An integer containing the bits to write.
	 * 
	 * @param numBits
	 *            The number of bits to write.
	 */
	public void write(final int data, final int numBits) {

		if (numBits < 32 && data >= (1 << numBits)) {
			throw new IllegalArgumentException(String.format("Truncating value %d to %d-bit integer", data, numBits));
		}

		for (int i = numBits - 1; i >= 0; i--) {

			// test bit
			boolean bit = (data >> i & 1) != 0;
			if (bit) {
				// set bit in current byte
				currentByte |= (1 << currentBitIndex);
			}

			// decrease current bit index
			--currentBitIndex;

			// check if current byte can be written
			if (currentBitIndex < 0) {
				writeCurrentByte();
			}
		}
	}

	/**
	 * Writes a sequence of bytes to the stream.
	 * 
	 * @param bytes
	 *            The sequence of bytes to write.
	 */
	public void writeBytes(final byte[] bytes) {

		// check if anything to do at all
		if (bytes == null)
			return;

		// are there bits left to write in buffer?
		if (currentBitIndex < Byte.SIZE - 1) {

			for (int i = 0; i < bytes.length; i++) {
				write(bytes[i], Byte.SIZE);
			}

		} else {

			// if bit buffer is empty, call can be delegated
			// to byte stream to increase
			byteStream.write(bytes, 0, bytes.length);
		}
	}
	
	/**
	 * Writes one byte to the stream.
	 * 
	 * @param b
	 *            The byte to be written.
	 */
	public void writeByte(final byte b) {
		writeBytes(new byte[] { b });
	}

	// Functions ///////////////////////////////////////////////////////////////

	/**
	 * Returns a byte array containing the sequence of bits written.
	 * 
	 * @return The byte array containing the written bits.
	 */
	public byte[] toByteArray() {

		// write any bits left in the buffer to the stream
		writeCurrentByte();

		// retrieve the byte array from the stream
		byte[] byteArray = byteStream.toByteArray();

		// reset stream for the sake of consistency
		byteStream.reset();

		// return the byte array
		return byteArray;
	}

	public void write(DatagramWriter data) {
		try {
			data.byteStream.writeTo(byteStream);
		} catch (IOException e) {
		}
	}

	public int size() {
		return byteStream.size();
	}

	/**
	 * Writes pending bits to the stream.
	 */
	public void writeCurrentByte() {

		if (currentBitIndex < Byte.SIZE - 1) {

			byteStream.write(currentByte);

			currentByte = 0;
			currentBitIndex = Byte.SIZE - 1;
		}
	}

	// Utilities ///////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		byte[] byteArray = byteStream.toByteArray();
		if (byteArray != null && byteArray.length != 0) {

			StringBuilder builder = new StringBuilder(byteArray.length * 3);
			for (int i = 0; i < byteArray.length; i++) {
				builder.append(String.format("%02X", 0xFF & byteArray[i]));

				if (i < byteArray.length - 1) {
					builder.append(' ');
				}
			}
			return builder.toString();
		} else {
			return "--";
		}
	}
}
