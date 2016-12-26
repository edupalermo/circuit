package org.circuit;

import java.util.concurrent.ThreadLocalRandom;

import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitScramble;
import org.circuit.generator.RandomGenerator;
import org.circuit.random.RandomWeight;
import org.circuit.solution.Solutions;
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
		methodChosser.add(20, Method.METHOD_RANDOM_ENRICH);
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
					newCircuit = RandomGenerator.randomGenerate(solutions.getInputSize(), random.nextInt(1, 1000));
					break;
				case METHOD_RANDOM_ENRICH:
					newCircuit = (Circuit) getCircuit();
					RandomGenerator.randomEnrich(newCircuit, 1 + random.nextInt(newCircuit.size()));
					break;
				case METHOD_CIRCUITS_SCRABLE:
					newCircuit = CircuitScramble.scramble(getCircuit(), getCircuit());
					break;
				default:
					throw new RuntimeException("Inconsistency");
				}

				ClientApplication.evaluateCircuit(newCircuit, solutions);
				putCircuit(newCircuit);
				
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
