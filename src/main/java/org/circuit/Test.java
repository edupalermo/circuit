package org.circuit;

import java.io.File;

import org.circuit.circuit.Circuit;
import org.circuit.evaluator.EvaluateHits;
import org.circuit.solution.StringSolution;
import org.circuit.util.IoUtils;

public class Test {
	
	public static void main(String args[]) {

		//Circuit circuit = IoUtils.readObject(new File("C:\\temp\\circuit\\better.obj"), Circuit.class);
		Circuit circuit = IoUtils.readObject(new File("/tmp/better.obj"), Circuit.class);

		int[] output = EvaluateHits.generateOutput(circuit);
		
		
		for (char c = 'a'; c <= 'z'; c++) {
			dump(c, circuit, output);
		}
		
		for (char c = 'A'; c <= 'Z'; c++) {
			dump(c, circuit, output);
		}
		
		for (char c = '0'; c <= '9'; c++) {
			dump(c, circuit, output);
		}
		
		dump('á', circuit, output);
		dump('à', circuit, output);
		dump('ã', circuit, output);
		dump('â', circuit, output);
		dump('0', circuit, output);
		
	}
	
	private static void dump(char c, Circuit circuit, int[] output) {
		
		String answer = null;
		
		try {
			answer = StringSolution.evaluate(circuit, output, Character.toString(c));
		} catch (Exception e) {
			answer = "error";
		}
		System.out.println(c + " [" + answer + "]");
		
	}

}
