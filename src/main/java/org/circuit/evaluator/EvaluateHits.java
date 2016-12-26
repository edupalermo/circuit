package org.circuit.evaluator;

import org.circuit.circuit.Circuit;
import org.circuit.solution.Solution;
import org.circuit.solution.Solutions;
import org.circuit.solution.TimeSlice;

public class EvaluateHits {
	
	public static int evaluate(Circuit circuit, Solutions solutions) {
		
		int score[][] = new int [circuit.size()][solutions.getOutputSize()];
		
		for (Solution solution: solutions) {
			evaluate(circuit, solution, score);
		}
		
		return sumBetterHits(score);
	}
	
	public static int[] generateOutput(Circuit circuit, Solutions solutions) {
		
		int score[][] = new int [circuit.size()][solutions.getOutputSize()];
		
		for (Solution solution: solutions) {
			evaluate(circuit, solution, score);
		}
		
		return generateOutput(score, solutions.getOutputSize());
	}
	
	
	private static int[] generateOutput(int score[][], int outputSize) {
		int[] output = new int[outputSize];
		for (int i = 0; i < score[0].length ; i++) {
			int better = 0;
			for (int j = 1; j < score.length ; j++) {
				if (score[j][i] > score[better][i]) {
					better = j;
				}
			}
			output[i] = better;
		}
		
		return output;
	}

	
	private static int sumBetterHits(int score[][]) {
		int sum = 0;
		for (int i = 0; i < score[0].length ; i++) {
			int better = 0;
			for (int j = 1; j < score.length ; j++) {
				if (score[j][i] > score[better][i]) {
					better = j;
				}
			}
			sum += score[better][i];
		}
		
		return sum;
	}

	private static void evaluate(Circuit circuit, Solution solution, int[][] score) {
		boolean state[] = new boolean[circuit.size()];
		circuit.reset();

		for (TimeSlice timeSlice : solution.getDialogue()) {
			circuit.assignInputToState(state, timeSlice.getInput());
			circuit.propagate(state);
			
			for (int i = 0; i < score.length; i++) {
				for (int j = 0; j < timeSlice.getOutput().size(); j++) {
					if (state[i] == timeSlice.getOutput().get(j).booleanValue()) {
						score[i][j]++;
					}
				}				
			}
		}
	}

}
