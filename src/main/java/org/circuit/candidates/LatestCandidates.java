package org.circuit.candidates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LatestCandidates implements CandidatesInterface {
	
	private static final long serialVersionUID = 1L;
	
	private boolean first = true;
	
	private List<Integer>[] array = null;
	private boolean finished[] = null;
	private int step[] = null;

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
		this.array = new List[size];
		for (int i = 0; i < size; i++) {
			this.array[i] = new ArrayList<Integer>();
		}
		this.finished = new boolean[size];
		this.step = new int[size];
	}
	
	public void populateInitialCandidates(boolean state[], List<Boolean> output) {
		if (this.array != null) {
			throw new RuntimeException("Inconsistency");
		}

		initiate(output.size());
		
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < output.size(); j++) {
				if (state[i] == output.get(j).booleanValue()) {
					array[j].add(i);
				}
			}
		}
	}

	
	public void check(boolean state[], List<Boolean> output) {
		if (array.length != output.size()) {
			throw new RuntimeException("Inconsistency");
		}
		
		for (int i = 0; i < array.length; i++) {
			
			if (!finished[i]) {
				Iterator<Integer> it = array[i].iterator();
				while (it.hasNext()) {
					int index = it.next();
					
					if (state[index] != output.get(i).booleanValue()) {
						
						if (array[i].size() == 1) {
							finished[i] = true;
							break;
						}
						else {
							it.remove();
						}
					}
				}
				step[i] ++;
			}
		}
	}

	private boolean hasOutputOutput() {
		boolean answer = true;

		for (List<Integer> l : array) {
			if (l.size() == 0) {
				answer = false;
				break;
			}
		}

		return answer;
	}

	public List<Integer> getBetterOutput() {
		ArrayList<Integer> output = null;

		if (hasOutputOutput()) {
			output = new ArrayList<Integer>();

			for (List<Integer> l : array) {
				output.add(l.get(0));
			}
		}
		return output;
	}

	public String dump() {
		StringBuilder sb = new StringBuilder();
		sb.append("Candidates: ");
		for (int i = 0; i < array.length; i++) {
			sb.append("[").append(step[i]).append("] ");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}


	@Override
	public boolean continueEvaluation() {
		boolean answer = false;
		
		for (boolean f : finished) {
			if (!f) {
				answer = true;
				break;
			}
		}
		
		return answer;
	}
	
	@Override
	public void intermediateDump(List<String> log) {
		
	}
	

}
