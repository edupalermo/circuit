package org.circuit.solution;

import java.util.Collections;
import java.util.List;

public class Solution {
	
	private final List<TimeSlice> dialogue;

	public Solution(List<TimeSlice> dialogue) {
		this.dialogue = Collections.unmodifiableList(dialogue);
	}

	public List<TimeSlice> getDialogue() {
		return dialogue;
	}
	
}
