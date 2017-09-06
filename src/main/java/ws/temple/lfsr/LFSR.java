package ws.temple.lfsr;

import java.nio.ByteBuffer;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public class LFSR {

	/**
	 * Array of xor masks yielding max-length LSFRs for bit-counts from 2 to 64. Values courtesy of:
	 * https://users.ece.cmu.edu/~koopman/lfsr/index.html
	 */
	protected static final long[] MAX_POLIES = {
		// @formatter:off
		/* 0*/ 0x0, /* INVALID */
		/* 1*/ 0x0, /* INVALID */
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
	 * Returns a maximum-length LSFR for the specified term length and starting state.
	 * 
	 * @param bits
	 *            The number of bits in the feedback term, between 2 and 64 (inclusive)
	 * @param start
	 *            The starting term, between 1 (inclusive) and 2^n - 1 (exclusive)
	 * @return A stream yielding each of the feedback terms for the specified LSFR
	 */
	public static LongStream maxLengthStream(int bits, long start) {
		final long size = (2L << bits - 1) - 1;

		if (bits < 2 || bits > 64)
			throw new IllegalArgumentException("Bit count must be between 2 and 64 (inclusive)");
		if (start < 1 || (size > 0 && start > size))
			throw new IllegalArgumentException("Beginning state out of range");

		if (size > 0) {
			return StreamSupport
				.longStream(new LSFRSpliterator(size, Spliterator.SIZED, MAX_POLIES[bits], start), false);
		}
		else {
			return StreamSupport.longStream(new LSFRSpliterator(MAX_POLIES[bits], start), false);
		}
	}

	/**
	 * Spliterator implementation that handles the actual processing of LSFR steps.
	 */
	protected static class LSFRSpliterator extends Spliterators.AbstractLongSpliterator {

		protected final long xorMask;
		protected long start;
		protected long lsfr;
		protected boolean done = false;

		protected LSFRSpliterator(long est, int additionalCharacteristics, long mask, long start) {
			super(est, additionalCharacteristics | Spliterator.DISTINCT);
			this.xorMask = mask;
			this.start = start;
			this.lsfr = start;
		}

		protected LSFRSpliterator(long mask, long start) {
			this(Long.MAX_VALUE, 0, mask, start);
		}

		@Override
		public boolean tryAdvance(LongConsumer action) {
			if (!done) {
				// Produce the term
				action.accept(lsfr);

				// Transition to the next state
				final int lsb = (int) (lsfr & 1);
				lsfr >>>= 1;
				if (lsb > 0) {
					lsfr ^= xorMask;
				}

				// If we've returned to the initial state, then there are no more terms to yield
				done = (lsfr == start);
				return true;
			}
			else {
				return false;
			}
		}

	}

	public static void main(String[] args) {
		// Scramble
		ByteBuffer source = ByteBuffer.wrap("demonstration".getBytes());
		byte[] scrambled = new byte[source.remaining()];

		LFSR.maxLengthStream(4, 1)
			.mapToInt(i -> (int) i - 1)
			.filter(i -> i < scrambled.length)
			.forEach(i -> scrambled[i] = source.get());

		System.out.println(new String(scrambled));

		// Unscramble
		ByteBuffer unscrambled = ByteBuffer.allocate(scrambled.length);

		LFSR.maxLengthStream(4, 1)
			.mapToInt(i -> (int) i - 1)
			.filter(i -> i < unscrambled.capacity())
			.forEach(i -> unscrambled.put(scrambled[i]));
		
		System.out.println(new String(unscrambled.array()));
	}

}
