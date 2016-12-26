package org.circuit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitScramble;
import org.circuit.comparator.CircuitComparator;
import org.circuit.evaluator.EvaluateHits;
import org.circuit.generator.RandomGenerator;
import org.circuit.random.RandomWeight;
import org.circuit.solution.Solutions;
import org.circuit.solution.StringSolution;
import org.circuit.util.CircuitUtils;
import org.circuit.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientApplication {

	private static final int POPULATION_SIZE = 10000;

	private static final long SECOND = 1000;
	private static final long MINUTE = 60 * SECOND;
	private static final long HOUR = 60 * MINUTE;

	private static final long SAVE_DELAY = 5 * MINUTE;
	private static final long SIMPLIFICATION_DELAY = HOUR;
	private static final long DUMP_DELAY = 10 * SECOND;

	private static final boolean validateConsistency = false;

	private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);
	
	public static final File FILE_BETTER = new File("./better.obj");
	
	private static final CircuitComparator circuitComparator = new CircuitComparator();
	
	public static Solutions solutions = new Solutions();
	
	static {
		solutions.add(new StringSolution("a", "vogal"));
		solutions.add(new StringSolution("b", "consoante"));
		solutions.add(new StringSolution("c", "consoante"));
		solutions.add(new StringSolution("d", "consoante"));
		solutions.add(new StringSolution("e", "vogal"));
		
		solutions.add(new StringSolution("h", "consoante"));
		
		solutions.add(new StringSolution("H", "consoante"));
		
		solutions.add(new StringSolution("0", "número"));
		
	}
	

	public static void main(String[] args) {
		
		List<Circuit> population = generateInitialRandomPopulation(solutions);
		
		if (FILE_BETTER.exists()) {
			Circuit newCircuit = IoUtils.readObject(FILE_BETTER, Circuit.class);
			evaluateCircuit(newCircuit, solutions);
			orderedAdd(population, newCircuit);
		}
		
		dumpPopulation(population, solutions);
		
		Circuit lastFirst = population.get(0);
		
		RandomWeight<Method> methodChosser = new RandomWeight<Method>();
		methodChosser.add(20, Method.METHOD_RANDOM_ENRICH);
		methodChosser.add(10, Method.METHOD_CIRCUITS_SCRABLE);
		methodChosser.add(1, Method.METHOD_RANDOM_CIRCUIT);
		
		for (;;) {
			
			logger.info("=============================================================");
			
			long initial = System.currentTimeMillis();
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			
			do {

				Circuit newCircuit = null;
				switch(methodChosser.next()) {
				case METHOD_RANDOM_CIRCUIT :
					newCircuit = RandomGenerator.randomGenerate(solutions.getInputSize(), population.get(0).size());
					break;
				case METHOD_RANDOM_ENRICH :
					newCircuit = (Circuit) population.get(random.nextInt(population.size())).clone();
					RandomGenerator.randomEnrich(newCircuit, 1 + random.nextInt(newCircuit.size()));
					break;
				case METHOD_CIRCUITS_SCRABLE :
					newCircuit = CircuitScramble.scramble(population.get(random.nextInt(population.size())), population.get(random.nextInt(population.size())));
					break;
/*				case 2 :
					newCircuit = population.get(random.nextInt(population.size()));
					CircuitUtils.simplify(newCircuit, EvaluateHits.generateOutput(newCircuit, solutions));
					break;
				case 3 :
					newCircuit = population.get(random.nextInt(population.size()));
					CircuitUtils.simplify(newCircuit, solutions);
					break;
*/				
					default:
						throw new RuntimeException("Inconsistency");
				}
				
				evaluateCircuit(newCircuit, solutions);
				orderedAdd(population, newCircuit);
				
			} while ((System.currentTimeMillis() - initial) < DUMP_DELAY);
			
			if (circuitComparator.compare(lastFirst, population.get(0)) != 0) {
				
				Circuit bestCircuit = (Circuit) population.get(0).clone();
				
				CircuitUtils.useLowerPortsWithSameOutput(bestCircuit, ClientApplication.solutions);
				CircuitUtils.simplify(bestCircuit, EvaluateHits.generateOutput(bestCircuit, ClientApplication.solutions));
				
				IoUtils.writeObject(FILE_BETTER, bestCircuit);
				
				evaluateCircuit(bestCircuit, solutions);
				orderedAdd(population, bestCircuit);

				lastFirst = population.get(0);
			}
			
			dumpPopulation(population, solutions);
			limitPopulation(population);
		}
	}
	
	public static void evaluateCircuit(Circuit circuit, Solutions solutions) {
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
			logger.info(String.format("[%5d] %s", i + 1, population.get(i).toSmallString()));
		}
		
		int limit = Math.min(POPULATION_SIZE, population.size());
		for (int i =  limit - 3; i < limit; i++) {
			logger.info(String.format("[%5d] %s", i + 1, population.get(i).toSmallString()));
		}
		logger.info(String.format("Population [%d] Total Hits [%d] Largest Circuit [%d]", population.size(), (solutions.getOutputSize() * CircuitUtils.getNumberOfSteps(solutions)), getLargest(population)));

	}
	
	public static int getLargest(List<Circuit> population) {
		int size = 0;
		
		for (Circuit c : population) {
			size = Math.max(c.size(), size);
		}
		
		return size;
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
