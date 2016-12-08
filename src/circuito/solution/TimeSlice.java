package circuito.solution;

import java.util.Collections;
import java.util.List;

public class TimeSlice {
	
	private final List<Boolean> input;
	private final List<Boolean> output;
	
	public TimeSlice(List<Boolean> input, List<Boolean> output) {
		this.input = Collections.unmodifiableList(input);
		this.output =  Collections.unmodifiableList(output);
	}

	public List<Boolean> getInput() {
		return input;
	}
	

	public List<Boolean> getOutput() {
		return output;
	}
	
}
