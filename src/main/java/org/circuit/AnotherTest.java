package org.circuit;

import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitScramble;
import org.circuit.evaluator.EvaluateHits;
import org.circuit.generator.RandomGenerator;
import org.circuit.util.CircuitUtils;

public class AnotherTest {
	
	public static void main(String args[]) {

		Circuit cs[] = new Circuit[5];
		
		cs[0] = RandomGenerator.randomGenerate(Application.solutions.getInputSize(), 1000);
		cs[1] = (Circuit) cs[0].clone();
		cs[2] = (Circuit) cs[0].clone();
		cs[3] = (Circuit) cs[0].clone();
		cs[4] = CircuitScramble.scramble(cs[0], cs[0]);
		
		for (Circuit c : cs) {
			Application.evaluateCircuit(c, Application.solutions);
		}
		
		for (int i = 0; i < cs.length; i++) {
			System.out.println(String.format("%d %s", i, cs[i].toSmallString()));
		}
		
		System.out.println("===============================================================");
		
		CircuitUtils.simplify(cs[1], Application.solutions);
		
		CircuitUtils.simplify(cs[2], EvaluateHits.generateOutput(cs[2], Application.solutions));
		
		CircuitUtils.useLowerPortsWithSameOutput(cs[3], Application.solutions);
		CircuitUtils.simplify(cs[3], EvaluateHits.generateOutput(cs[3], Application.solutions));
		

		for (Circuit c : cs) {
			Application.evaluateCircuit(c, Application.solutions);
		}
		
		for (int i = 0; i < cs.length; i++) {
			System.out.println(String.format("%d %s", i, cs[i].toSmallString()));
		}
		
		
	}

}
