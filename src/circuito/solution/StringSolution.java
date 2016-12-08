package circuito.solution;

import java.util.ArrayList;
import java.util.List;

import circuito.Clock;

public class StringSolution extends Solution {
	
	private final static byte BYTE_ZERO = 0x00;
	
	public StringSolution(String input, String output) {
		super(stringsToTimeSliceList(input, output));
	}
	
	private static List<TimeSlice> stringsToTimeSliceList(String input, String output) {
		List<TimeSlice> listTimeSlice = new ArrayList<TimeSlice>();
		
		Clock clock = new Clock();
		
		for (byte b : input.getBytes()) {
			List<Boolean> listInput = byteToList(b);
			listInput.add(clock.thick());
			listInput.add(true);
			
			List<Boolean> listOutput = byteToList(BYTE_ZERO);
			listOutput.add(false);
			
			listTimeSlice.add(new TimeSlice(listInput, listOutput));
		}

		for (byte b : output.getBytes()) {
			List<Boolean> listInput = byteToList(BYTE_ZERO);
			listInput.add(clock.thick());
			listInput.add(false);
			
			List<Boolean> listOutput = byteToList(b);
			listOutput.add(true);
			
			listTimeSlice.add(new TimeSlice(listInput, listOutput));
		}

		return listTimeSlice;
	}
	
	
	private static List<Boolean> byteToList(byte b) {
		List<Boolean> list = new ArrayList<Boolean>();
		for (int i = 0; i < 8; i++) {
			list.add(getBit(b, i));
		}
		return list;
	}
	
	private static boolean getBit(byte b, int i) {
		return (0x01 & (b >> (7 - i))) == 1;
	}

}
