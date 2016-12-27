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
import org.circuit.evaluator.EvaluateHits;
import org.circuit.generator.RandomGenerator;
import org.circuit.solution.Solutions;
import org.circuit.util.CircuitUtils;
import org.circuit.util.IoUtils;
import org.circuit.util.RandomUtils;
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
		}
		else {
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
			for (int i =  limit - 3; i < limit; i++) {
				logger.info(String.format("[%5d] %s", i + 1, population.get(i).toSmallString()));
			}
		}

		DecimalFormat myFormatter = new DecimalFormat("###,###");
		logger.info(String.format("Population [%d] Total Hits [%d] Weight [%s] Largest Circuit [%d] ", population.size(), (Solutions.getInstance().getOutputSize() * CircuitUtils.getNumberOfSteps(Solutions.getInstance())), myFormatter.format(getWeight()), getLargest()));
		
	}
	
	public String randomPick() {
		return IoUtils.objectToBase64(this.population.get(RandomUtils.raffle(this.population.size())));
	}
	
	public void insert(String base64Circuit) {
		Circuit circuit = (Circuit) IoUtils.base64ToObject(base64Circuit);
		int pos = this.orderedAdd(circuit);
		
		if (pos == 0) {
			Circuit bestCircuit = (Circuit) circuit.clone();


			if (circuit.size() > 6000) {
				CircuitUtils.simplifyByRemovingUnsedPorts(bestCircuit);
			}
			else {
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
		int pos = -1;
		synchronized (this.population) {
			pos = Collections.binarySearch(population, newCircuit, circuitComparator);
			if (pos < 0) {
				pos = ~pos;
				population.add(pos, newCircuit);
			}
			else {
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


}
