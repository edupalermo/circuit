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

	public static int toInt(byte array[], int offset, int size) {
		int answer = 0;
		
		for (int i = offset; i < (offset + size); i++) {
			answer += (array[i] & 0xFF) * Math.pow(256, (size - 1) - (i - offset));
		}
		
		return answer;
	}

	public static byte[] toByteArray(int input) {
		int size = bytesNeededToRepresent(input);
		
		byte[] answer = new byte[size];
		
		for (int i = 0; i < size; i++) {
			answer[i] = (byte) ((input / Math.pow(256, i)) % 256);
		}
		
		return answer;
	}

}
