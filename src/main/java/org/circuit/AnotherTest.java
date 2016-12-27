package org.circuit;

import org.circuit.circuit.Circuit;
import org.circuit.circuit.CircuitScramble;
import org.circuit.evaluator.EvaluateHits;
import org.circuit.generator.RandomGenerator;
import org.circuit.util.CircuitUtils;

public class AnotherTest {
	
	public static void main(String args[]) {
		System.out.println("Initiating ");


		Circuit original = RandomGenerator.randomGenerate(8000);

		Circuit cs[] = new Circuit[5];
		long took[] = new long[5];


		//First
		long initial = System.currentTimeMillis();
		cs[0] = (Circuit) original.clone();
		CircuitUtils.evaluateCircuit(cs[0]);
		took[0] = System.currentTimeMillis() - initial;

		//Second
		initial = System.currentTimeMillis();
		cs[1] = (Circuit) original.clone();
		CircuitUtils.simplify(cs[1]);
		CircuitUtils.evaluateCircuit(cs[1]);
		took[1] = System.currentTimeMillis() - initial;

		//Third
		initial = System.currentTimeMillis();
		cs[2] = (Circuit) original.clone();
		CircuitUtils.simplifyByRemovingUnsedPorts(cs[2]);
		CircuitUtils.evaluateCircuit(cs[2]);
		took[2] = System.currentTimeMillis() - initial;

		//Fourth
		initial = System.currentTimeMillis();
		cs[3] = (Circuit) original.clone();
		CircuitUtils.betterSimplify(cs[3]);
		CircuitUtils.evaluateCircuit(cs[3]);
		took[3] = System.currentTimeMillis() - initial;

		//Last
		initial = System.currentTimeMillis();
		cs[4] =  CircuitScramble.scramble((Circuit) cs[0].clone(), (Circuit) cs[0].clone());
		CircuitUtils.evaluateCircuit(cs[4]);
		took[4] = System.currentTimeMillis() - initial;

		for (int i = 0; i < cs.length; i++) {
			System.out.println(String.format("%d [%dms] %s", i, took[i], cs[i].toSmallString()));
		}
		
		
	}

}
