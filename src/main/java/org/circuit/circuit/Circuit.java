package org.circuit.circuit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.circuit.Application;
import org.circuit.Generator;
import org.circuit.list.PersistedList;
import org.circuit.port.Port;
import org.circuit.port.PortAnd;
import org.circuit.port.PortInput;
import org.circuit.port.PortMemory;
import org.circuit.port.PortNot;
import org.circuit.port.PortOr;
import org.circuit.solution.Solution;
import org.circuit.solution.TimeSlice;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Circuit extends PersistedList<Port> {

	private static final long serialVersionUID = 1L;

	private transient Set<Port> discarded = new TreeSet<Port>();

	private transient Generator generator = null;

	private transient long validated = 0;

	private transient static final Logger logger = LoggerFactory.getLogger(Application.class);
	
	private boolean validateConsistency = false;

	public Circuit(int size) {
		for (int i = 0; i < size; i++) {
			this.add(new PortInput(i));
		}
		this.generator = new Generator(0, size);
	}

	public Circuit(TimeSlice timeSlice) {
		this(timeSlice.getInput().size());
	}

	public void randomEnrich(List<Solution> solutions) {
		boolean stop = false;
		do {
			Port newPort = generatePort();
			if (available(newPort)) {
				this.add(newPort);

				if (this.haveUniqueResponse(solutions, this.size() - 1)) {
					stop = true;
				} else {
					this.discarded.add(this.remove(this.size() - 1));
				}
			}
		} while (!stop);
	}

	public void orderedEnrich(int size) {
		Port newPort = generator.next(size);
		this.add(newPort);
	}

	private boolean available(Port newPort) {
		for (Port port : this) {
			if (port.equals(newPort)) {
				return false;
			}
		}

		return !this.discarded.contains(newPort);
	}

	private Port generatePort() {
		Port port = null;

		int array[] = new int[4];

		array[0] = (this.size() * (this.size() - 1)) / 2;
		array[1] = (this.size() * (this.size() - 1)) / 2;
		array[2] = this.size();
		array[3] = this.size();

		switch (pick(array)) {
		case 0:
			port = PortAnd.random(this.size());
			break;
		case 1:
			port = PortOr.random(this.size());
			break;
		case 2:
			port = PortMemory.random(this.size());
			break;
		case 3:
			port = PortNot.random(this.size());
			break;
		default:
			throw new RuntimeException("Inconsistency!");
		}
		return port;
	}

	private int pick(int array[]) {
		int sum = 0;
		for (int i : array) {
			sum += i;
		}

		Random random = new Random();
		int generated = random.nextInt(sum);

		sum = 0;

		int answer = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
			if (generated <= sum) {
				answer = i;
				break;
			}
		}
		return answer;
	}

	public boolean haveUniqueResponse(List<Solution> solutions, int index) {
		boolean answer = false;

		IntGapList same = new IntGapList();
		try {

			same = Application.intGapListPool.borrowObject();

			for (int i = 0; i < index; i++) {
				same.add(i);
			}

			for (Solution solution : solutions) {
				evaluateRepetition(same, solution, index);
				if (same.size() == 0)
					break;
			}

			answer = same.size() == 0;

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				Application.intGapListPool.returnObject(same);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return answer;
	}

	public void removeDuplicatePorts(List<Solution> solutions) {

		int originalSize = this.size();
		
		//logger.info("Before simplifications: " + this.toString());
		
		
		if (this.validated == 0) { // Remover os inputs das validacoes
			while (this.get((int) this.validated) instanceof PortInput) {
				this.validated++;
			}
		}

		try {

			Map<Long, List<Long>> same = new TreeMap<Long, List<Long>>(Collections.reverseOrder());

			for (Solution solution : solutions) {
				evaluateRepetition(same, solution);
				if (same.size() == 0)
					break;
			}

			for (Map.Entry<Long, List<Long>> entry : same.entrySet()) {
				if ((entry.getValue()== null) || (entry.getValue().size() != 0)) {
					//logger.info(String.format("Removing port [%d]", entry.getKey().intValue()));
					this.removePort(entry.getKey().intValue());
				}
				
				
				if (entry.getValue() != null) {
					entry.getValue().clear();
				}
			}

			same.clear();

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			this.validated = this.size();
		}

		//logger.info("After simplifications: " + this.toString());
		
		// logger.info(String.format("Original size [%d] after simplification [%d]", originalSize, this.size()));

	}

	public void evaluateRepetition(Map<Long, List<Long>> same, Solution solution) {
		boolean state[] = new boolean[this.size()];
		this.reset();

		for (TimeSlice timeSlice : solution.getDialogue()) {
			assignInputToState(state, timeSlice.getInput());
			propagate(state);

			boolean firstTime = same.size() == 0;

			for (int i = (int) this.validated; i < state.length; i++) {
				List<Long> list = same.get(Long.valueOf(i));
				if (list == null) {
					list = new ArrayList<Long>();
					same.put(Long.valueOf(i), list);
				}

				if (firstTime) {
					for (int j = 0; j < i; j++) {
						if (state[i] == state[j]) {
							list.add(Long.valueOf(j));
						}
					}
				} else {
					Iterator<Long> it = list.iterator();
					while (it.hasNext()) {
						int j = it.next().intValue();
						if (state[i] != state[j]) {
							it.remove();
						}
					}
					
				}
			}
		}
	}

	public void evaluateRepetition(IntGapList same, Solution solution, int index) {
		boolean state[] = new boolean[this.size()];
		this.reset();

		for (TimeSlice timeSlice : solution.getDialogue()) {
			assignInputToState(state, timeSlice.getInput());
			propagate(state);

			for (int i = (same.size() - 1); i >= 0; i--) {
				int j = same.get(i);
				if (state[index] != state[j]) {
					same.remove(i);
					if (same.size() == 0) {
						return; // Nothing to simplyfy
					}
				}
			}
		}
	}

	public List<Boolean> generateInitialState() {
		List<Boolean> state = new ArrayList<Boolean>();
		for (int i = 0; i < this.size(); i++) {
			state.add(false);
		}
		return state;
	}

	public void assignInputToState(boolean state[], List<Boolean> input) {
		for (int i = 0; i < input.size(); i++) {
			state[i] = input.get(i).booleanValue();
		}
	}

	public void propagate(boolean state[]) {
		for (int i = 0; i < this.size(); i++) {
			state[i] = this.get(i).evaluate(state);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.size(); i++) {
			sb.append("[").append(i).append(" ").append(this.get(i).toString()).append("] ");
		}
		return sb.toString();
	}

	public void reset() {
		for (Port port : this) {
			port.reset();
		}
	}

	public void simplify(List<Integer> output) {

		Set<Port> canRemove = new TreeSet<Port>();

		// Adding all ports!
		for (Port port : this) {
			if (!(port instanceof PortInput)) {
				canRemove.add(port);
			}
		}

		// Removing port that can't be removed
		for (int i = 0; i < output.size(); i++) {
			Port it = this.get(output.get(i));
			simplify(canRemove, it);
		}

		TreeSet<Integer> sortedIndex = new TreeSet<Integer>();
		for (Port port : canRemove) {
			sortedIndex.add(this.indexOf(port));
		}

		for (Integer index : sortedIndex.descendingSet()) {
			removePort(index.intValue());
		}

		this.generator = new Generator(0, this.size());
	}

	private void removePort(int index) {
		//logger.info(String.format("Checking [%d] size [%d]", index, size()));
		for (int i = size() -1; i >= index + 1; i--) {
			//logger.info(String.format("Checking [%d] %s", index, get(i).toString()));
			if (get(i).references(index)) {
				logger.info(String.format("Recurring on [%d]", i));
				removePort(i);
			}
		}
		
		for (int i = index + 1; i < size(); i++) {
			this.get(i).adustLeft(index);
		}
		//logger.info("Removing port: " + this.get(index).toString());
		this.remove(index);
	}
	
	private void markToRemoveReferences(Map<Long, List<Long>> same, int index) {
		
		for (int i = index + 1; i < size(); i++) {
			if (get(i).references(index)) {
				logger.info(String.format("Marking [%d] for removal", i));
				//if (same.get(Long.valueOf(i)) != null) {
					same.put(Long.valueOf(i), null);
					markToRemoveReferences(same, i);
				//}
			}
		}

	}

	private void simplify(Set<Port> canRemove, Port port) {
		if (!(port instanceof PortInput)) {
			canRemove.remove(port);

			if (port instanceof PortAnd) {
				simplify(canRemove, this.get(((PortAnd) port).getMinor()));
				simplify(canRemove, this.get(((PortAnd) port).getMajor()));
			} else if (port instanceof PortOr) {
				simplify(canRemove, this.get(((PortOr) port).getMinor()));
				simplify(canRemove, this.get(((PortOr) port).getMajor()));
			} else if (port instanceof PortNot) {
				simplify(canRemove, this.get(((PortNot) port).getIndex()));
			} else if (port instanceof PortMemory) {
				simplify(canRemove, this.get(((PortMemory) port).getIndex()));
			}

		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.discarded = new TreeSet<Port>();
		this.generator = new Generator(0, this.size());
	}

}
