package circuito.circuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.magicwerk.brownies.collections.primitive.IntGapList;

import circuito.Application;
import circuito.port.Port;
import circuito.port.PortAnd;
import circuito.port.PortInput;
import circuito.port.PortMemory;
import circuito.port.PortNot;
import circuito.port.PortOr;
import circuito.solution.Solution;
import circuito.solution.TimeSlice;

public class Circuit extends ArrayList<Port> {
	
	private static final long serialVersionUID = 1L;
	
	private volatile Set<Port> discarded = new TreeSet<Port>();
	
	public Circuit(int size) {
		for (int i = 0; i < size; i++) {
			this.add(new PortInput(i));
		}
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
				
				if (this.haveUniqueResponse(solutions, this.size() -1)) {
					stop = true;
				}
				else {
					this.discarded.add(this.remove(this.size() - 1));
				}
			}
		} while (!stop);
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
		
		array[0] = (this.size()*(this.size() - 1)) / 2; 
		array[1] = (this.size()*(this.size() - 1)) / 2; 
		array[2] = this.size(); 
		array[3] = this.size();
		
		switch(pick(array)) {
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
			}
			finally {
				try {
					Application.intGapListPool.returnObject(same);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		
			return answer;
	}

	
	public void evaluateRepetition(IntGapList same, Solution solution, int index) {
		List<Boolean> state = this.generateInitialState();
		this.reset();
		
		for (TimeSlice timeSlice : solution.getDialogue()) {
			assignInputToState(state, timeSlice.getInput());
			propagate(state);
			
			for (int i = (same.size() -1); i >= 0; i--) {
				int j = same.get(i);
				if (state.get(index).booleanValue() != state.get(j).booleanValue()) {
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
		for (int i =0; i < this.size(); i++) {
			state.add(false);
		}
		return state;
	}

	public void assignInputToState(List<Boolean> state, List<Boolean> input) {
		for (int i = 0; i < input.size(); i++) {
			state.set(i, input.get(i));
		}
	}
	
	public void propagate(List<Boolean> state) {
		for (int i = 0; i < this.size(); i++) {
			state.set(i, this.get(i).evaluate(state));
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i =0; i < this.size(); i++) {
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
	}

	private void removePort(int index) {
		for (int i = index + 1; i < size(); i++) {
			this.get(i).adustLeft(index);
		}
		this.remove(index);
	}

	private void simplify(Set<Port> canRemove, Port port) {
		if (!(port instanceof PortInput)) {
			canRemove.remove(port);

			if (port instanceof PortAnd) {
				simplify(canRemove, this.get(((PortAnd) port).getMinor()));
				simplify(canRemove, this.get(((PortAnd) port).getMajor()));
			}
			else if (port instanceof PortOr) {
				simplify(canRemove, this.get(((PortOr) port).getMinor()));
				simplify(canRemove, this.get(((PortOr) port).getMajor()));
			}
			else if (port instanceof PortNot) {
				simplify(canRemove, this.get(((PortNot) port).getIndex()));
			}
			else if (port instanceof PortMemory) {
				simplify(canRemove, this.get(((PortMemory) port).getIndex()));
			}

		}
	}
	
}
