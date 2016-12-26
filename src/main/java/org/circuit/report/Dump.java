package org.circuit.report;

import org.circuit.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Dump {
	
	@Autowired
	private Environment environment;
	
	@Scheduled(fixedRate = 10000)
    public void report() {
		environment.dump();
		environment.limitPopulation();
    }

}
