package org.circuit.util;

public class ByteUtils {
	
	public static int bytesNeededToRepresent(long n) {
		if (n < 0) {
			throw new RuntimeException("Inconsistency");
		}
		if (n == 0) {
			return 1;
		}
		return (int) (1 + (Math.log(n)/Math.log(256)));
		
	}

}
