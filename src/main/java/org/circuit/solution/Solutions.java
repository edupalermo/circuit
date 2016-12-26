package org.circuit.solution;

import java.util.ArrayList;

public class Solutions extends ArrayList<Solution> {

	private static final long serialVersionUID = 1L;

	public int getInputSize() {
		return this.get(0).getDialogue().get(0).getInput().size();
	}
	
	public int getOutputSize() {
		return this.get(0).getDialogue().get(0).getOutput().size();
	}
	

}
