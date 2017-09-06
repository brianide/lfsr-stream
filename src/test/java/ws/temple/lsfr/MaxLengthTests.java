package ws.temple.lsfr;

import org.junit.Test;

import static org.junit.Assert.*;

public class MaxLengthTests {
	
	@Test
	public void inRange() {
		for(int i = 2; i <= 8; i++) {
			assertEquals(LSFR.maxLengthStream(i, 1).count(), (2 << i - 1) - 1);
		}
		
		assertEquals(LSFR.maxLengthStream(63, 1).count(), 62);
	}

}
