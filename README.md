## About

**lfsr-stream** is an implementation of a linear-feedback shift register on the Java 8 Stream API.
It is most useful as a PRNG, where the sequence it produces will traverse a certain sequence in
psuedorandom order, cycling without repetition. When used with a maximum-length feedpack polynomial,
these property make it useful eg. for deterministically shuffling/traversing a series of items in
psuedorandom order.

## Example

```
/* Scramble */
ByteBuffer source = ByteBuffer.wrap("demonstration".getBytes());
byte[] scrambled = new byte[source.remaining()];

LSFR.maxLengthStream(4, 1)
	.mapToInt(i -> (int) i - 1)
	.filter(i -> i < scrambled.length)
	.forEach(i -> scrambled[i] = source.get());
	
System.out.println(new String(scrambled));

/* Unscramble */
ByteBuffer unscrambled = ByteBuffer.allocate(scrambled.length);

LSFR.maxLengthStream(4, 1)
	.mapToInt(i -> (int) i - 1)
	.filter(i -> i < scrambled.length)
	.forEach(i -> unscrambled.put(scrambled[i]));
	
System.out.println(new String(unscrambled.array()));
```
