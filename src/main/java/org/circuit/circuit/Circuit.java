package org.circuit.circuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.circuit.Application;
import org.circuit.port.Port;
import org.circuit.port.PortInput;
import org.circuit.solution.Solution;
import org.circuit.solution.TimeSlice;
import org.circuit.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Circuit extends ArrayList<Port> implements Cloneable {

	private static final long serialVersionUID = 1L;

	private transient static final Logger logger = LoggerFactory.getLogger(Application.class);
	
	private boolean validateConsistency = false;
	
	private TreeMap<String, Integer> grades = new TreeMap<String, Integer>();
	
	public static final String GRADE_HIT = "GRADE_HIT";
	public static final String GRADE_CIRCUIT_SIZE = "GRADE_CIRCUIT_SIZE";
	
	private Circuit() {
		
	}

	public Circuit(int size) {
		for (int i = 0; i < size; i++) {
			this.add(new PortInput(i));
		}
	}

	public Circuit(TimeSlice timeSlice) {
		this(timeSlice.getInput().size());
	}

	
	public void setGrade(String name, Integer grade) {
		this.grades.put(name, grade);
	}

	public Integer getGrade(String name) {
		return this.grades.get(name);
	}

	public List<Boolean> generateInitialState() {
		List<Boolean> state = new ArrayList<Boolean>();
		for (int i = 0; i < this.size(); i++) {
			state.add(false);
		}
		return state;
	}

	public void assignInputToState(boolean state[], List<Boolean> input) {
		for (int i = 0; i < input.size(); i++) {
			state[i] = input.get(i).booleanValue();
		}
	}

	public void propagate(boolean state[]) {
		for (int i = 0; i < this.size(); i++) {
			state[i] = this.get(i).evaluate(state);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(this.toSmallString());
		sb.append(" ");
		
		for (int i = 0; i < this.size(); i++) {
			sb.append("[").append(i).append(" ").append(this.get(i).toString()).append("] ");
		}

		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	public String toSmallString() {
		StringBuffer sb = new StringBuffer();
		
		for (Map.Entry<String, Integer> entry : this.grades.entrySet()) {
			sb.append("[");
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue().toString());
			sb.append("] ");
		}
		
		sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
	}

	public void reset() {
		for (Port port : this) {
			port.reset();
		}
	}

	public void removePort(int index) {
		//logger.info(String.format("Checking [%d] size [%d]", index, size()));
		for (int i = size()-1; i >= index + 1; i--) {
			//logger.info(String.format("Checking [%d] %s", index, get(i).toString()));
			if (get(i).references(index)) {
				//logger.info(String.format("Recurring on [%d]", i));
				throw new RuntimeException("Inconsistency");
			}
		}
		
		for (int i = index + 1; i < size(); i++) {
			this.get(i).adustLeft(index);
		}
		//logger.info("Removing port: " + this.get(index).toString());
		this.remove(index);
	}
	
	public void evaluate(List<Solution> solutions, int outputSize) {
		
		int score[][] = new int [this.size()][outputSize];
		
		for (Solution solution: solutions) {
			evaluate(solution, score);
		}
		
		for (int i = 0; i < score.length; i++) {
			System.out.println(String.format("%d - %s", i , ArrayUtils.toString(score[i])));
		}
		
		System.out.println("Score: " + dumpScore(score));
		
	}
	
	public String dumpScore(int score[][]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < score[0].length ; i++) {
			int better = 0;
			int betterScore = score[0][i];
			for (int j = 0; j < score.length ; j++) {
				if (score[j][i] > score[better][i]) {
					better = j;
				}
			}
			sb.append(String.format("[%d %d %d] ", i, better, score[better][i]));
		}
		
		return sb.toString();
	}

	public void evaluate(Solution solution, int[][] score) {
		boolean state[] = new boolean[this.size()];
		this.reset();

		for (TimeSlice timeSlice : solution.getDialogue()) {
			assignInputToState(state, timeSlice.getInput());
			propagate(state);
			
			for (int i = 0; i < score.length; i++) {
				for (int j = 0; j < timeSlice.getOutput().size(); j++) {
					if (state[i] == timeSlice.getOutput().get(j).booleanValue()) {
						score[i][j]++;
					}
				}				
			}
			

		}

	}

	@Override
	public Object clone() {
		Circuit circuit = new Circuit();
		for (Port port : this) {
			circuit.add((Port) port.clone());
		}
		
		return circuit;
	}
	

	
	
}
