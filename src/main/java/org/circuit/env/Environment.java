package org.circuit.env;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.circuit.circuit.Circuit;
import org.circuit.comparator.CircuitComparator;
import org.circuit.generator.RandomGenerator;
import org.circuit.solution.Solutions;
import org.circuit.util.CircuitUtils;
import org.circuit.util.IoUtils;
import org.circuit.util.RandomUtils;
import org.circuit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Environment {

	private static final Logger logger = LoggerFactory.getLogger(Environment.class);

	@Value("${folder.circuit}")
	private String folderName;

	private final static int POPULATION_SIZE = 10000;

	private List<Circuit> population = new ArrayList<Circuit>();

	private static final CircuitComparator circuitComparator = new CircuitComparator();

	@PostConstruct
	public void postConstruct() {
		File file = new File(folderName, "better.obj");

		Circuit circuit = null;
		if (file.exists()) {
			logger.info("File with old better circuit exists!");
			circuit = IoUtils.readObject(file, Circuit.class);
		} else {
			logger.info(String.format("File not found [%s] generating random circuit", file.getAbsolutePath()));
			circuit = RandomGenerator.randomGenerate(500);
		}

		CircuitUtils.evaluateCircuit(circuit);
		this.orderedAdd(circuit);

	}

	public void dump() {
		logger.info("====================================================================");
		for (int i = 0; i < Math.min(30, population.size()); i++) {
			logger.info(String.format("[%5d] %s", i + 1, population.get(i).toSmallString()));
		}

		if (population.size() > 3) {
			int limit = Math.min(POPULATION_SIZE, population.size());
			for (int i = limit - 3; i < limit; i++) {
				logger.info(String.format("[%5d] %s", i + 1, population.get(i).toSmallString()));
			}
		}

		DecimalFormat myFormatter = new DecimalFormat("###,###");
		logger.info(String.format("Population [%d] Total Hits [%d] Weight [%s] Largest Circuit [%d] %s", population.size(), (Solutions.getInstance().getOutputSize() * CircuitUtils.getNumberOfSteps(Solutions.getInstance())), myFormatter.format(getWeight()), getLargest(), getRenewDump()));

	}

	public String randomPick() {
		return this.population.get(RandomUtils.raffle(this.population.size())).getCachedBase64();
	}

	public void insert(String base64Circuit) {
		Circuit circuit = (Circuit) IoUtils.base64ToObject(base64Circuit);
		int pos = this.orderedAdd(circuit);

		if (pos == 0) {
			Circuit bestCircuit = (Circuit) circuit.clone();

			if (circuit.size() > 6000) {
				CircuitUtils.simplifyByRemovingUnsedPorts(bestCircuit);
			} else {
				CircuitUtils.betterSimplify(bestCircuit);
			}

			CircuitUtils.useLowerPortsWithSameOutput(bestCircuit);
			CircuitUtils.simplify(bestCircuit);

			File file = new File(folderName, "better.obj");
			IoUtils.writeObject(file, bestCircuit);

			CircuitUtils.evaluateCircuit(bestCircuit);
			this.orderedAdd(bestCircuit);
		}
	}

	private int orderedAdd(Circuit newCircuit) {
		
		if (newCircuit == null) {
			throw new RuntimeException("Cannot add null circuit!");
		}
		
		int pos = -1;
		synchronized (this.population) {
			pos = Collections.binarySearch(population, newCircuit, circuitComparator);
			if (pos < 0) {
				pos = ~pos;
				population.add(pos, newCircuit);
			} else {
				pos = -1;
			}
		}
		return pos;
	}

	public void limitPopulation() {
		synchronized (population) {
			while (population.size() > POPULATION_SIZE) {
				population.remove(population.size() - 1);
			}
		}
	}

	private int getLargest() {
		int size = 0;
		synchronized (population) {
			for (Circuit c : population) {
				size = Math.max(c.size(), size);
			}
		}
		return size;
	}

	private int getWeight() {
		int size = 0;
		synchronized (population) {
			for (Circuit c : population) {
				size += c.size();
			}
		}
		return size;
	}

	public void adjust() {
		if (getWeight() > 30000000) {
			logger.warn("Adjust enabled!");
			for (int i = 0; i < 1 + ((getWeight() - 30000000) / 5000000); i++) {
				Circuit newCircuit = (Circuit) this.population.get(0).clone();
				RandomGenerator.randomEnrich(newCircuit, 1);
				CircuitUtils.evaluateCircuit(newCircuit);
				this.orderedAdd(newCircuit);
			}

		}
	}

	public void renewDna() {

		if (population.size() > 10) {

			Circuit first = population.get(0);
			Circuit last = population.get(Math.min(population.size(), POPULATION_SIZE) - 1);

			
			int distance = StringUtils.getLevenshteinDistance(first.toString(), last.toString());
			
			if (distance < getRenewThreshold() && (first.getGrade(Circuit.GRADE_HIT).intValue() == last.getGrade(Circuit.GRADE_HIT).intValue())) {
				System.out.println("Renewing .........................................");
				synchronized (population) {
					while (population.size() > 2) {
						population.remove(population.size() - 1);
					}
				}

				int firstSize = first.size();
				ThreadLocalRandom random = ThreadLocalRandom.current();
				
				long initial = System.currentTimeMillis();
				
				while ((System.currentTimeMillis() - initial) < 9000) {
					Circuit newCircuit = RandomGenerator.randomGenerate(firstSize + random.nextInt(firstSize));
					CircuitUtils.evaluateCircuit(newCircuit);
					this.orderedAdd(newCircuit);
				}
			}
		}

	}

	private String getRenewDump() {

		StringBuffer sb = new StringBuffer();

		if (population.size() > 10) {

			Circuit first = population.get(0);
			Circuit last = population.get(Math.min(population.size(), POPULATION_SIZE) - 1);

			int actualDistance = StringUtils.getLevenshteinDistance(first.toString(), last.toString());

			sb.append(String.format("Renew on %d actual %d", getRenewThreshold(), actualDistance));

		} else {
			sb.append("Population to few to evaluate");
		}

		return sb.toString();
	}
	
	private int getRenewThreshold() {
		Circuit first = population.get(0);
		return 7 * Math.max(first.toString().length() / 5, 10);
	}

}
