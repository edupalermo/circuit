package org.circuit.solution;

import java.util.ArrayList;

public class Solutions extends ArrayList<Solution> {

	private static final Object semaphore = new Object();

	private static Solutions instance = null;

	private static final long serialVersionUID = 1L;
	
	private Solutions() {
		this.add(new StringSolution("a", "vogal"));
		this.add(new StringSolution("b", "consoante"));
		this.add(new StringSolution("c", "consoante"));
		this.add(new StringSolution("d", "consoante"));
		this.add(new StringSolution("e", "vogal"));
		
		this.add(new StringSolution("h", "consoante"));
		this.add(new StringSolution("m", "consoante"));
		
		this.add(new StringSolution("A", "vogal"));
		this.add(new StringSolution("H", "consoante"));
		
		this.add(new StringSolution("0", "número"));
		this.add(new StringSolution("1", "número"));

	}

	public static Solutions getInstance() {
		if (instance == null) {
			synchronized (semaphore) {
				if (instance == null) {
					instance = new Solutions();
				}
			}
		}
		return instance;
	}

	public int getInputSize() {
		return this.get(0).getDialogue().get(0).getInput().size();
	}
	
	public int getOutputSize() {
		return this.get(0).getDialogue().get(0).getOutput().size();
	}
	

}
