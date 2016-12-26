package org.circuit.solution;

import java.util.ArrayList;

public class Solutions extends ArrayList<Solution> {

	private static final long serialVersionUID = 1L;
	
	public Solutions() {
		this.add(new StringSolution("a", "vogal"));
		this.add(new StringSolution("b", "consoante"));
		this.add(new StringSolution("c", "consoante"));
		this.add(new StringSolution("d", "consoante"));
		this.add(new StringSolution("e", "vogal"));
		
		this.add(new StringSolution("h", "consoante"));
		
		this.add(new StringSolution("H", "consoante"));
		
		this.add(new StringSolution("0", "número"));

	}

	public int getInputSize() {
		return this.get(0).getDialogue().get(0).getInput().size();
	}
	
	public int getOutputSize() {
		return this.get(0).getDialogue().get(0).getOutput().size();
	}
	

}
