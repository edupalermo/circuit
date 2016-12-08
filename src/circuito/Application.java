package circuito;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.magicwerk.brownies.collections.primitive.IntGapList;

import circuito.circuit.Circuit;
import circuito.solution.Solution;
import circuito.solution.StringSolution;
import circuito.solution.TimeSlice;

public class Application {
	
	public static ObjectPool<IntGapList> intGapListPool = new GenericObjectPool<IntGapList>(new IntGapListFactory());
	static{
		((GenericObjectPool)intGapListPool).setMaxTotal(-1);
	}

	public static void main(String[] args) {
		
		List<Solution> solutions = new ArrayList<Solution>();
		
		solutions.add(new StringSolution("a", "vogal"));
		solutions.add(new StringSolution("b", "consoante"));
		solutions.add(new StringSolution("c", "consoante"));
		
		Circuit circuit = generateCircuit(solutions);
		System.out.println("Generates circuit size: " + circuit.size());

	}
	
	public static Circuit generateCircuit(List<Solution> solutions) {
		
		Circuit circuit = new Circuit(solutions.get(0).getDialogue().get(0)); 
		
		List<Integer> output = null;
		
		while ((output =  getOutput(circuit, solutions)) == null) {
			int originalSize = circuit.size();
			// circuit.enrich(oldSize);
			// circuit.simplifySmart(solutions, oldSize);
			circuit.randomEnrich(solutions);
		}
		
		return circuit;
	}
	
	
	private static List<Integer> getOutput(Circuit circuit, List<Solution> solutions) {
		System.out.println();
		System.out.println();
		System.out.println(String.format("Solutions to process %d circuit size %d", solutions.size(), circuit.size()));
		System.out.println("Circuit: " + circuit.toString());
		
		Candidates candidates = new Candidates();

		for (Solution solution : solutions) {
			evaluate(circuit, solution, candidates);
			if (!candidates.canProvideOutput()) {
				break;
			}
			else {
				System.out.println("#solution closed#");
			}
		}
		
		return candidates.getOutput();
	}
	
	private static void evaluate(Circuit circuit, Solution solution, Candidates candidates)  {
		
		List<Boolean> state = circuit.generateInitialState();
		for (TimeSlice timeSlice : solution.getDialogue()) {
			System.out.println("Input: " + booleanListToString(timeSlice.getInput()) + " Output: " + booleanListToString(timeSlice.getOutput()));
			circuit.assignInputToState(state, timeSlice.getInput());
			circuit.propagate(state);
			System.out.println("State: " + booleanListToString(state));
			candidates.compute(state, timeSlice.getOutput());
			
			candidates.dump();
			
			if (candidates.canProvideOutput()) {
				System.out.println("Current Output: " + integerListToString(candidates.getOutput()));
			}
			else {
				break;
			}
		}

	}
	
	
	
	
	
	public static String booleanListToString(List<Boolean> list) {
		StringBuilder sb = new StringBuilder();
		for (boolean b : list) {
			sb.append(b ? "[Y] " : "[N] ");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	public static String integerListToString(List<Integer> list) {
		StringBuilder sb = new StringBuilder();
		for (Integer i : list) {
			sb.append("[").append(i.toString()).append("] ");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
}
