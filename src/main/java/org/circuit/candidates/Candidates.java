package org.circuit.candidates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Candidates extends ArrayList<List<Integer>> implements CandidatesInterface {
	
	private static final long serialVersionUID = 1L;
	
	private boolean first = true;

	public void compute(boolean state[], List<Boolean> output) {
		if (first) {
			first = false;
			populateInitialCandidates(state, output);
		}
		else {
			check(state, output);
		}
	}


	private void initiate(int size) {
		for (int i = 0; i < size; i++) {
			this.add(new ArrayList<Integer>());
		}
	}
	
	public void populateInitialCandidates(boolean state[], List<Boolean> output) {
		if (this.size() > 0) {
			throw new RuntimeException("Inconsistency");
		}

		initiate(output.size());
		
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < output.size(); j++) {
				if (state[i] == output.get(j).booleanValue()) {
					get(j).add(i);
				}
			}
		}
	}

	
	public void check(boolean state[], List<Boolean> output) {
		if (this.size() != output.size()) {
			throw new RuntimeException("Inconsistency");
		}
		
		for (int i = 0; i < size(); i++) {
			Iterator<Integer> it = this.get(i).iterator();
			while (it.hasNext()) {
				int index = it.next();
				
				if (state[index] != output.get(i).booleanValue()) {
					it.remove();
				}
			}
		}
	}
	
	public boolean continueEvaluation() {
		for (List<Integer> l : this) {
			if (l.size() == 0) {
				return false;
			}
		}
		return true;
	}


	private boolean hasOutputOutput() {
		boolean answer = true;

		for (List<Integer> l : this) {
			if (l.size() == 0) {
				answer = false;
				break;
			}
		}

		return answer;
	}



	public List<Integer> getOutput() {
		ArrayList<Integer> output = null;

		if (hasOutputOutput()) {
			output = new ArrayList<Integer>();

			for (List<Integer> l : this) {
				output.add(l.get(0));
			}
		}
		return output;
	}

	public void intermediateDump(List<String> log) {
		StringBuilder sb = new StringBuilder();
		sb.append("Candidates: ");
		for (List<Integer> l : this) {
			sb.append("[").append(l.size()).append("] ");
		}
		sb.deleteCharAt(sb.length() - 1);
		log.add(sb.toString());
	}

}
