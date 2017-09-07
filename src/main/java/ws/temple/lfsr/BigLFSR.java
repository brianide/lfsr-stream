package ws.temple.lfsr;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BigLFSR {

	/**
	 * Returns an iterator over the sequence of terms produced by the described LFSR.
	 * 
	 * @param mask
	 *            The xor mask describing the "taps" polynomial
	 * @param start
	 *            The starting term, between 1 (inclusive) and 2^n - 1 (exclusive)
	 * 
	 * @return
	 */
	public static Iterator<BigInteger> iterator(BigInteger mask, BigInteger start) {
		checkArgs(mask, start);
		return new LFSRIterator(mask.toByteArray(), start.toByteArray());
	}

	/**
	 * Returns a Stream yielding the sequence of terms produced by the described LFSR.
	 * 
	 * @param bits
	 *            The xor mask describing the "taps" polynomial
	 * @param start
	 *            The starting term, between 1 (inclusive) and 2^n - 1 (exclusive)
	 * 
	 * @return
	 */
	public static Stream<BigInteger> stream(BigInteger mask, BigInteger start) {
		final Iterator<BigInteger> iter = iterator(mask, start);
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.DISTINCT), false);
	}

	protected static void checkArgs(BigInteger mask, BigInteger start) {
		if (mask.signum() < 1 || start.signum() < 1)
			throw new IllegalArgumentException("Mask and starting state must be positive values");

		if (start.bitLength() > mask.bitLength())
			throw new IllegalArgumentException("Starting state out of range");
	}

	public static class LFSRIterator implements Iterator<BigInteger> {

		protected final byte[] mask;
		protected byte[] init;
		protected byte[] lfsr;
		protected boolean done = false;

		protected LFSRIterator(byte[] mask, byte[] start) {
			this.mask = mask;

			this.init = new byte[mask.length];
			System.arraycopy(start, 0, init, init.length - start.length, start.length);

			this.lfsr = Arrays.copyOf(init, init.length);
		}

		@Override
		public boolean hasNext() {
			return !done;
		}

		@Override
		public BigInteger next() {
			if (done)
				throw new NoSuchElementException();

			// Save the current state
			final BigInteger last = new BigInteger(lfsr);

			// Transition LFSR the next state
			rightShift(lfsr);
			if (last.testBit(0))
				xor(lfsr, mask);

			// If we've returned to the initial state, then there are no more terms to yield
			done = Arrays.equals(lfsr, init);

			return last;
		}

		protected static void xor(byte[] lfsr, byte[] mask) {
			for (int i = 0; i < lfsr.length; i++)
				lfsr[i] ^= mask[i];
		}

		protected static void rightShift(byte[] lfsr) {
			int carry;
			int nextCarry = 0;

			for (int i = 0; i < lfsr.length; i++) {
				carry = nextCarry;
				nextCarry = (lfsr[i] & 1) << 7;

				lfsr[i] = (byte) ((0xFF & lfsr[i]) >>> 1 | carry);
			}
		}

	}

}
