package org.circuit.solution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.circuit.Clock;
import org.circuit.circuit.Circuit;

public class StringSolution extends Solution {

	private final static byte BYTE_ZERO = 0x00;

	private final static int BIT_ANSWER = 8;

	public StringSolution(String input, String output) {
		super(stringsToTimeSliceList(input, output));
	}

	private static List<TimeSlice> stringsToTimeSliceList(String input, String output) {
		List<TimeSlice> listTimeSlice = new ArrayList<TimeSlice>();

		Clock clock = new Clock();

		try {
			for (byte b : input.getBytes("UTF-8")) {
				List<Boolean> listInput = byteToList(b);
				listInput.add(clock.thick());
				listInput.add(true); // Always true
				listInput.add(false); // Always false
				listInput.add(true); // Speaking

				List<Boolean> listOutput = byteToList(BYTE_ZERO);
				listOutput.add(false);

				listTimeSlice.add(new TimeSlice(listInput, listOutput));
			}

			for (byte b : output.getBytes("UTF-8")) {
				List<Boolean> listInput = byteToList(BYTE_ZERO);
				listInput.add(clock.thick());
				listInput.add(true); // Always true
				listInput.add(false); // Always false
				listInput.add(false); // Hearing

				List<Boolean> listOutput = byteToList(b);
				listOutput.add(true);

				listTimeSlice.add(new TimeSlice(listInput, listOutput));
			}

		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		// Final comunication

		List<Boolean> listInput = byteToList(BYTE_ZERO);
		listInput.add(clock.thick());
		listInput.add(true); // Always true
		listInput.add(false); // Always false
		listInput.add(false); // Hearing

		List<Boolean> listOutput = byteToList(BYTE_ZERO);
		listOutput.add(false);

		listTimeSlice.add(new TimeSlice(listInput, listOutput));

		return listTimeSlice;
	}

	private static List<Boolean> byteToList(byte b) {
		List<Boolean> list = new ArrayList<Boolean>();
		for (int i = 0; i < 8; i++) {
			list.add(getBit(b & 0xFF, i));
		}
		return list;
	}

	private static byte listToByte(boolean state[], int output[], int offset, int size) {
		int answer = 0;

		for (int i = 0; i < size; i++) {
			if (state[output[i + offset]]) {
				answer = answer + (int) Math.pow(2, size - 1 - i);
			}
		}
		return (byte) answer;
	}

	private static boolean getBit(int b, int i) {
		return (0x01 & (b >> (7 - i))) == 1;
	}

	public static String evaluate(Circuit circuit, int[] output, String input) {
		Clock clock = new Clock();

		boolean state[] = new boolean[circuit.size()];
		circuit.reset();

		try {
			for (byte b : input.getBytes("UTF-8")) {
				List<Boolean> listInput = byteToList(b);
				listInput.add(clock.thick());
				listInput.add(true); // Always true
				listInput.add(false); // Always false
				listInput.add(true); // Speaking

				circuit.assignInputToState(state, listInput);
				circuit.propagate(state);

				if (state[output[BIT_ANSWER]]) {
					throw new RuntimeException("Inconsistency");
				}
			}
		} catch (UnsupportedEncodingException e1) {
			throw new RuntimeException(e1);
		}

		String answer = null;
		ByteArrayOutputStream baos = null;
		int count = 0;

		try {
			baos = new ByteArrayOutputStream();
			do {
				List<Boolean> listInput = byteToList(BYTE_ZERO);
				listInput.add(clock.thick());
				listInput.add(true); // Always true
				listInput.add(false); // Always false
				listInput.add(false); // Hearing

				circuit.assignInputToState(state, listInput);
				circuit.propagate(state);

				count++;

				if (count > 100) {
					state[output[BIT_ANSWER]] = false;
					// throw new RuntimeException("Inconsistency");
				}

				if (state[output[BIT_ANSWER]]) {
					baos.write(listToByte(state, output, 0, 8));
				}
			} while (state[output[BIT_ANSWER]]);

			answer = new String(baos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return new String(answer);
	}

	public static void main(String args[]) {

		List<Boolean> listInput = byteToList((byte) -44);

		boolean state[] = new boolean[listInput.size()];

		for (int i = 0; i < listInput.size(); i++) {
			System.out.println(String.format("%d %s", i, listInput.get(i).toString()));
			state[i] = listInput.get(i).booleanValue();
		}

		System.out.println(listToByte(state, new int[] { 0, 1, 2, 3, 4, 5, 6, 7 }, 0, 8));
	}

}
