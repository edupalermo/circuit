package org.circuit;

import java.util.concurrent.ThreadLocalRandom;

import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitScramble;
import org.circuit.evaluator.EvaluateHits;
import org.circuit.generator.RandomGenerator;
import org.circuit.random.RandomWeight;
import org.circuit.solution.Solutions;
import org.circuit.util.CircuitUtils;
import org.circuit.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Client {

	private static RestTemplate restTemplate = new RestTemplate();
	
	private static final Logger logger = LoggerFactory.getLogger(Client.class);

	public static void main(String[] args) {

		SpringApplication springApplication = new SpringApplicationBuilder().sources(Client.class).web(false).bannerMode(Mode.OFF).build();

		springApplication.run(args);

		RandomWeight<Method> methodChosser = new RandomWeight<Method>();
		methodChosser.add(10000, Method.METHOD_RANDOM_ENRICH);
		methodChosser.add(10, Method.METHOD_CIRCUITS_SCRABLE);
		methodChosser.add(1, Method.METHOD_RANDOM_CIRCUIT);

		Solutions solutions = new Solutions();

		for (;;) {


			ThreadLocalRandom random = ThreadLocalRandom.current();

			for (;;) {
				
				long initial = System.currentTimeMillis();

				Circuit newCircuit = null;
				
				Method method = methodChosser.next();
				switch (method) {
				case METHOD_RANDOM_CIRCUIT:
					newCircuit = RandomGenerator.randomGenerate(solutions.getInputSize(), random.nextInt(1, 250));
					break;
				case METHOD_RANDOM_ENRICH:
					newCircuit = (Circuit) getCircuit();
					RandomGenerator.randomEnrich(newCircuit, 1 + random.nextInt(newCircuit.size() / 10));
					break;
				case METHOD_CIRCUITS_SCRABLE:
					Circuit c1 = getCircuit();
					Circuit c2 = getCircuit();
					
					logger.info(String.format("Method SCRAMBLE %d %d", c1.size(), c2.size()));
					if (c1.size() + c2.size() < 1500) {
						if (c1.size() + c2.size() > 500) {
							CircuitUtils.useLowerPortsWithSameOutput(c1, ClientApplication.solutions);
							CircuitUtils.simplify(c1, EvaluateHits.generateOutput(c1, ClientApplication.solutions));
							
							CircuitUtils.useLowerPortsWithSameOutput(c2, ClientApplication.solutions);
							CircuitUtils.simplify(c2, EvaluateHits.generateOutput(c2, ClientApplication.solutions));
						}
						
						newCircuit = CircuitScramble.scramble(getCircuit(), getCircuit());
						
					}
					else {
						logger.info(String.format("Skiping scramble. %d", c1.size() + c2.size()));
						continue;
					}
					
					break;
				default:
					throw new RuntimeException("Inconsistency");
				}

				ClientApplication.evaluateCircuit(newCircuit, solutions);
				putCircuit(newCircuit);
				
				newCircuit.clear();
				newCircuit = null;
				
				logger.info(String.format("Method %s took %d ms", method.name(), (System.currentTimeMillis() - initial)));

			}
		}
	}

	private static Circuit getCircuit() {
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/circuit/random", String.class);

		return (Circuit) IoUtils.base64ToObject(response.getBody());
	}

	private static void putCircuit(Circuit circuit) {
		restTemplate.postForLocation("http://localhost:8080/circuit", IoUtils.objectToBase64(circuit));
	}

}
