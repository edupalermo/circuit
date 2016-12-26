package org.circuit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.circuit.circuit.Circuit;
import org.circuit.comparator.CircuitComparator;
import org.circuit.evaluator.EvaluateHits;
import org.circuit.generator.RandomGenerator;
import org.circuit.solution.Solutions;
import org.circuit.solution.StringSolution;
import org.circuit.util.CircuitUtils;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

	private static final int POPULATION_SIZE = 10000;

	private static final long SECOND = 1000;
	private static final long MINUTE = 60 * SECOND;
	private static final long HOUR = 60 * MINUTE;

	private static final long SAVE_DELAY = 5 * MINUTE;
	private static final long SIMPLIFICATION_DELAY = HOUR;
	private static final long DUMP_DELAY = 10 * SECOND;

	private static final boolean validateConsistency = false;

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static ObjectPool<IntGapList> intGapListPool = new GenericObjectPool<IntGapList>(new IntGapListFactory());
	
	private static final CircuitComparator circuitComparator = new CircuitComparator();

	static {
		((GenericObjectPool) intGapListPool).setMaxTotal(-1);
	}

	public static void main(String[] args) {

		Solutions solutions = new Solutions();

		solutions.add(new StringSolution("a", "vogal"));
		solutions.add(new StringSolution("b", "consoante"));
		solutions.add(new StringSolution("c", "consoante"));
		solutions.add(new StringSolution("d", "consoante"));
		solutions.add(new StringSolution("e", "vogal"));
		
		List<Circuit> population = generateInitialRandomPopulation(solutions);
		Collections.sort(population, circuitComparator);
		dumpPopulation(population, solutions);
		
		Circuit lastFirst = population.get(0);
		
		for (;;) {
			
			logger.info("=============================================================");
			
			long initial = System.currentTimeMillis();
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			
			do {

				Circuit newCircuit = null;
				switch(random.nextInt(2)) {
				case 0 :
					newCircuit = RandomGenerator.randomGenerate(solutions.getInputSize(), population.get(0).size());
					break;
				case 1 :
					newCircuit = (Circuit) population.get(random.nextInt(population.size())).clone();
					RandomGenerator.randomEnrich(newCircuit, 1 + (newCircuit.size() / 10));
					break;
/*				case 2 :
					newCircuit = population.get(random.nextInt(population.size()));
					CircuitUtils.simplify(newCircuit, EvaluateHits.generateOutput(newCircuit, solutions));
					break;
				case 3 :
					newCircuit = population.get(random.nextInt(population.size()));
					CircuitUtils.simplify(newCircuit, solutions);
					break;
					default:
						throw new RuntimeException("Inconsistency");
*/				
				}
				
				evaluateCircuit(newCircuit, solutions);
				orderedAdd(population, newCircuit);
				
			} while ((System.currentTimeMillis() - initial) < DUMP_DELAY);
			
			if (circuitComparator.compare(lastFirst, population.get(0)) != 0) {
				
				Circuit newCircuit = (Circuit) population.get(0).clone();
				CircuitUtils.simplify(newCircuit, EvaluateHits.generateOutput(newCircuit, solutions));
				evaluateCircuit(newCircuit, solutions);
				
				orderedAdd(population, newCircuit);

				lastFirst = population.get(0);
			}
			
			dumpPopulation(population, solutions);
			limitPopulation(population);
		}
	}
	
	private static void evaluateCircuit(Circuit circuit, Solutions solutions) {
		circuit.setGrade(Circuit.GRADE_HIT, EvaluateHits.evaluate(circuit, solutions));
		circuit.setGrade(Circuit.GRADE_CIRCUIT_SIZE, circuit.size());
	}
	
	private static void orderedAdd(List<Circuit> population, Circuit newCircuit) {
		int pos = Collections.binarySearch(population, newCircuit, circuitComparator);
		if (pos < 0) {
			population.add(~pos, newCircuit);
		}
	}

	public static void limitPopulation(List<Circuit> population) {
		while (population.size() > POPULATION_SIZE) {
			population.remove(population.size() - 1);
		}
	}

	public static List<Circuit> generateInitialRandomPopulation(Solutions solutions) {
		List<Circuit> population = new ArrayList<Circuit>();

		long initial = System.currentTimeMillis();

		do {
			Circuit newCircuit = RandomGenerator.randomGenerate(solutions.getInputSize(), 250);
			evaluateCircuit(newCircuit, solutions);
			orderedAdd(population, newCircuit);


		} while ((System.currentTimeMillis() - initial) < DUMP_DELAY);

		return population;
	}

	public static void dumpPopulation(List<Circuit> population, Solutions solutions) {
		for (int i = 0; i < 30; i++) {
			logger.info(String.format("[%2d] %s", i + 1, population.get(i).toSmallString()));
		}
		logger.info(String.format("Population [%d] Total Hits [%d]", population.size(), (solutions.getOutputSize() * CircuitUtils.getNumberOfSteps(solutions))));

	}


	private static String getPercent(long initial) {
		double percent = 100d * (double) (System.currentTimeMillis() - initial) / (double) SAVE_DELAY;
		return String.format("%3.3f%%", percent);
	}

	public static String booleanListToString(List<Boolean> list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			sb.append("[").append(Integer.toString(i));
			if (list.get(i).booleanValue()) {
				sb.append(" Y");
			} else {
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
