package ws.temple.lsfr;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.*;

import ws.temple.lfsr.LFSR;

public class MaxLengthTests {
	
	// TODO Get a beefier rig to run 64-bit LSFR tests on
	
	@Test
	public void checkPeriod() {
		for(int i = 2; i <= 16; i++) {
			assertEquals(LFSR.maxLengthStream(i, 1).count(), (2 << i - 1) - 1);
		}
	}
	
	@Test
	public void noDuplicate() {
		boolean[] checks = new boolean[(2 << 16 - 1) - 1];
		Arrays.fill(checks, false);
		
		LFSR.maxLengthStream(16, 1).mapToInt(i -> (int) i - 1).forEach(i -> {
			assertFalse(checks[i]);
			checks[i] = true;
		});
		
		for(boolean check : checks) {
			assertTrue(check);
		}
	}

}
