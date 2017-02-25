package org.circuit.candidates;

import java.util.List;

public interface CandidatesInterface {
	
	void compute(boolean state[], List<Boolean> output);

	public boolean continueEvaluation();

	public void intermediateDump(List<String> log);


}
