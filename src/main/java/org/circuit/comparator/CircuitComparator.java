package org.circuit.comparator;

import java.util.Comparator;

import org.circuit.circuit.Circuit;

public class CircuitComparator implements Comparator<Circuit> {

	@Override
	public int compare(Circuit c1, Circuit c2) {

		int answer = c2.getGrade(Circuit.GRADE_HIT).compareTo(c1.getGrade(Circuit.GRADE_HIT));
		if (answer != 0) {
			return answer;
		}

		answer = c1.getGrade(Circuit.GRADE_CIRCUIT_SIZE).compareTo(c2.getGrade(Circuit.GRADE_CIRCUIT_SIZE));
		if (answer != 0) {
			return answer;
		}
		
		return c1.toString().compareTo(c2.toString());
	}

}
