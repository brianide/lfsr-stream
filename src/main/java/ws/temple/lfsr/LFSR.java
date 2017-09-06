package ws.temple.lfsr;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public class LFSR {

	/**
	 * Array of xor masks yielding max-length LFSRs for bit-counts from 2 to 64. Values courtesy of:
	 * https://users.ece.cmu.edu/~koopman/lfsr/index.html
	 */
	protected static final long[] MAX_POLIES = {
		// @formatter:off
		/* 0*/ /* INVALID */
		/* 1*/ /* INVALID */
		/* 2*/ 0x3L,
		/* 3*/ 0x6L,
		/* 4*/ 0x9L,
		/* 5*/ 0x12L,
		/* 6*/ 0x21L,
		/* 7*/ 0x41L,
		/* 8*/ 0x8EL,
		/* 9*/ 0x108L,
		/*10*/ 0x204L,
		/*11*/ 0x402L,
		/*12*/ 0x829L,
		/*13*/ 0x100DL,
		/*14*/ 0x2015L,
		/*15*/ 0x4001L,
		/*16*/ 0x8016L,
		/*17*/ 0x10004L,
		/*18*/ 0x20013L,
		/*19*/ 0x40013L,
		/*20*/ 0x80004L,
		/*21*/ 0x100002L,
		/*22*/ 0x200001L,
		/*23*/ 0x400010L,
		/*24*/ 0x80000DL,
		/*25*/ 0x1000004L,
		/*26*/ 0x2000023L,
		/*27*/ 0x4000013L,
		/*28*/ 0x8000004L,
		/*29*/ 0x10000002L,
		/*30*/ 0x20000029L,
		/*31*/ 0x40000004L,
		/*32*/ 0x80000057L,
		/*33*/ 0x100000029L,
		/*34*/ 0x200000073L,
		/*35*/ 0x400000002L,
		/*36*/ 0x80000003BL,
		/*37*/ 0x100000001FL,
		/*38*/ 0x2000000031L,
		/*39*/ 0x4000000008L,
		/*40*/ 0x800000001CL,
		/*41*/ 0x10000000004L,
		/*42*/ 0x2000000001FL,
		/*43*/ 0x4000000002CL,
		/*44*/ 0x80000000032L,
		/*45*/ 0x10000000000DL,
		/*46*/ 0x200000000097L,
		/*47*/ 0x400000000010L,
		/*48*/ 0x80000000005BL,
		/*49*/ 0x1000000000038L,
		/*50*/ 0x200000000000EL,
		/*51*/ 0x4000000000025L,
		/*52*/ 0x8000000000004L,
		/*53*/ 0x10000000000023L,
		/*54*/ 0x2000000000003EL,
		/*55*/ 0x40000000000023L,
		/*56*/ 0x8000000000004AL,
		/*57*/ 0x100000000000016L,
		/*58*/ 0x200000000000031L,
		/*59*/ 0x40000000000003DL,
		/*60*/ 0x800000000000001L,
		/*61*/ 0x1000000000000013L,
		/*62*/ 0x2000000000000034L,
		/*63*/ 0x4000000000000001L,
		/*64*/ 0x800000000000000DL,
		// @formatter:on
	};

	/**
	 * Returns an iterator over the sequence of terms produced by the described LFSR.
	 * 
	 * @param bits
	 *            The number of bits in the feedback term, between 2 and 64 (inclusive)
	 * @param start
	 *            The starting term, between 1 (inclusive) and 2^n - 1 (exclusive)
	 * 
	 * @return
	 */
	public static PrimitiveIterator.OfLong maxLengthIterator(int bits, long start) {
		checkArgs(bits, start);
		return new LFSRIterator(MAX_POLIES[bits - 2], start);
	}
	
	/**
	 * Returns a Stream yielding the sequence of terms produced by the described LFSR.
	 * 
	 * @param bits
	 *            The number of bits in the feedback term, between 2 and 64 (inclusive)
	 * @param start
	 *            The starting term, between 1 (inclusive) and 2^n - 1 (exclusive)
	 * 
	 * @return
	 */
	public static LongStream maxLengthStream(int bits, long start) {
		final PrimitiveIterator.OfLong iter = maxLengthIterator(bits, start);
		return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(iter, Spliterator.DISTINCT),
				false);
	}

	protected static void checkArgs(int bits, long start) {
		if (bits < 2 || bits > 64)
			throw new IllegalArgumentException("Bit count must be between 2 and 64 (inclusive)");

		final int highBit = Long.numberOfTrailingZeros(Long.highestOneBit(start) + 1);
		if (highBit > bits)
			throw new IllegalArgumentException("Starting state out of range");
	}

	public static class LFSRIterator implements PrimitiveIterator.OfLong {

		protected final long mask;
		protected long start;
		protected long lfsr;
		protected boolean done = false;

		protected LFSRIterator(long mask, long start) {
			this.mask = mask;
			this.start = start;
			this.lfsr = start;
		}

		@Override
		public boolean hasNext() {
			return !done;
		}

		@Override
		public long nextLong() {
			if (done)
				throw new NoSuchElementException();

			// Save the current state
			final long last = lfsr;

			// Transition LFSR the next state
			lfsr >>>= 1;
			if ((last & 1) > 0)
				lfsr ^= mask;

			// If we've returned to the initial state, then there are no more terms to yield
			done = (lfsr == start);

			return last;
		}

	}
	
}
