package org.circuit.util;

public class ArrayUtils {
	
	public static String toString(int[] array) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < array.length; i++) {
			sb.append(String.format("[%d - %d] ", i , array[i]));
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
		
	}

}
