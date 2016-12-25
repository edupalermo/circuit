package org.circuit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.circuit.candidates.Candidates;
import org.circuit.candidates.CandidatesInterface;
import org.circuit.candidates.LatestCandidates;
import org.circuit.circuit.Circuit;
import org.circuit.period.Period;
import org.circuit.solution.Solution;
import org.circuit.solution.StringSolution;
import org.circuit.solution.TimeSlice;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

	private static final File CIRCUIT_FILE = new File("circuit.obj");
	
	private static final long SECOND = 1000; 
	private static final long MINUTE = 60 * SECOND; 
	private static final long HOUR = 60 * MINUTE;

	private static final long SAVE_DELAY = 5 * MINUTE;
	private static final long SIMPLIFICATION_DELAY = HOUR;
	private static final long DUMP_DELAY = 10 * SECOND;
	
	private static final boolean validateConsistency = false;
	
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static ObjectPool<IntGapList> intGapListPool = new GenericObjectPool<IntGapList>(new IntGapListFactory());
	static{
		((GenericObjectPool)intGapListPool).setMaxTotal(-1);
	}

	public static void main(String[] args) {
		
		List<Solution> solutions = new ArrayList<Solution>();
		
		solutions.add(new StringSolution("a", "vogal"));
		solutions.add(new StringSolution("b", "consoante"));
		solutions.add(new StringSolution("c", "consoante"));
		solutions.add(new StringSolution("d", "consoante"));
		solutions.add(new StringSolution("e", "vogal"));
		
		Circuit circuit = generateCircuit(solutions);
		System.out.println("Generates circuit size: " + circuit.size());

	}
	
	public static Circuit generateCircuit(List<Solution> solutions) {

		Circuit circuit = null;

		//if (CIRCUIT_FILE.exists()) {
		//	circuit = IoUtils.readObject(CIRCUIT_FILE, Circuit.class);
		//	LatestCandidates candidates = (LatestCandidates) getLastOutput(circuit, solutions);
		//	circuit.simplify(candidates.getBetterOutput());
		//	log.add(String.format("%s Circuit: %d ", candidates.dump(), circuit.size()));
		//}
		//else {
			circuit = new Circuit(solutions.get(0).getDialogue().get(0));
		//}
		
		List<Integer> output = null;

		long initial = System.currentTimeMillis();
		
		Period save = new Period(SAVE_DELAY);
		Period simplify = new Period(SIMPLIFICATION_DELAY);
		Period dump = new Period(DUMP_DELAY);

		while ((output =  getOutput(circuit, solutions, getPercent(initial))) == null) {
			//circuit.randomEnrich(solutions);
			
			int size = circuit.size();
			for (int i = 0; i < (size / 2); i++) {
				circuit.orderedEnrich(size);
			}
			
			if (validateConsistency) {
				for (int k = 0; k < circuit.size(); k++) {
					if (!circuit.get(k).checkConsistency(k)) {
						throw new RuntimeException(String.format("Inconsistency [%d] %s ",k , circuit.get(k).toString()));
					}
				}
			}

			
			circuit.removeDuplicatePorts(solutions);

			if (validateConsistency) {
				for (int k = 0; k < circuit.size(); k++) {
					if (!circuit.get(k).checkConsistency(k)) {
						throw new RuntimeException(String.format("Inconsistency [%d] %s ",k , circuit.get(k).toString()));
					}
				}
			}
			
			//if (save.alarm()) {
			//	IoUtils.writeObject(CIRCUIT_FILE, circuit);
			//	log.add("Saving circuit");
			//}
			
			//if (simplify.alarm()) {
			//	int oldSize = circuit.size();
			//	LatestCandidates candidates = (LatestCandidates) getLastOutput(circuit, solutions);
			//	circuit.simplify(candidates.getBetterOutput());
			//	log.add(String.format("Smplifying circuit [%d] - [%d]", oldSize, circuit.size()));
			//}
			

			//if (dump.alarm()) {
				LatestCandidates candidates = (LatestCandidates) getLastOutput(circuit, solutions);
				logger.info(String.format("%s Circuit: %d ", candidates.dump(), circuit.size()));
			//}

		}


		
		return circuit;
	}
	
	private static String getPercent(long initial) {
		double percent = 100d * (double)(System.currentTimeMillis() - initial) / (double)SAVE_DELAY;
		return String.format("%3.3f%%", percent);
	}
	
	
	private static List<Integer> getOutput(Circuit circuit, List<Solution> solutions, String percent) {
		//log.add("\n");
		//log.add(String.format("Solutions to process %d circuit size %d simplification %s", solutions.size(), circuit.size(), percent));
		//System.out.println("Circuit: " + circuit.toString());
		
		Candidates candidates = new Candidates();

		for (int i = 0; i < solutions.size(); i++) {
			//System.out.println(String.format("Working on solution [%d]", i));
			Solution solution = solutions.get(i);
			evaluate(i, circuit, solution, candidates, true);
			if (!candidates.continueEvaluation()) {
				break;
			}
			else {
				//log.add("#solution closed#");
			}
		}
		
		return candidates.getOutput();
	}


	private static CandidatesInterface getLastOutput(Circuit circuit, List<Solution> solutions) {
		LatestCandidates candidates = new LatestCandidates();

		for (int i = 0; i < solutions.size(); i++) {
			Solution solution = solutions.get(i);
			evaluate(i, circuit, solution, candidates, false);
			if (!candidates.continueEvaluation()) {
				break;
			}
		}

		return candidates;
	}



	private static void evaluate(int solutionIndex, Circuit circuit, Solution solution, CandidatesInterface candidates, boolean dump)  {
		
		boolean state[] = new boolean[circuit.size()];
		circuit.reset();
		for (int i = 0; i < solution.getDialogue().size(); i++) {
			if (dump) {
				// log.add(String.format("Working on solution [%d] on Dialogue [%d]", solutionIndex, i));
			}

			TimeSlice timeSlice = solution.getDialogue().get(i);

			//System.out.println("Input: " + booleanListToString(timeSlice.getInput()) + " Output: " + booleanListToString(timeSlice.getOutput()));
			circuit.assignInputToState(state, timeSlice.getInput());
			circuit.propagate(state);
			//System.out.println("State: " + booleanListToString(state));
			candidates.compute(state, timeSlice.getOutput());
			
			if (dump) {
				//candidates.intermediateDump(log);
			}
			
			if (candidates.continueEvaluation()) {
				//System.out.println("Current Output: " + integerListToString(candidates.getOutput()));
			}
			else {
				break;
			}
		}

	}
	
	
	
	
	
	public static String booleanListToString(List<Boolean> list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i<list.size(); i++) {
			sb.append("[").append(Integer.toString(i));
			if (list.get(i).booleanValue()) {
				sb.append(" Y");
			}
			else {
				sb.append(" N");
			}
			sb.append("] ");
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
