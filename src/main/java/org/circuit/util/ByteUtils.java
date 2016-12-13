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

	public static int toInt(byte array[]) {
		int answer = 0;
		
		for (int i = 0; i < array.length; i++) {
			answer += array[i] * Math.pow(256, (array.length - 1) - i);
		}
		
		return answer;
	}

	public static byte[] toByteArray(int input, int size) {
		byte[] answer = null;
		
		if (size < bytesNeededToRepresent(input)) {
			throw new RuntimeException("Inconsistency");
		}
		
		answer = new byte[size];
		
		for (int i = 0; i < size; i++) {
			answer[i] = (byte) (Math.pow(256, size - i) % input);
		}
		
		return answer;
	}

}
