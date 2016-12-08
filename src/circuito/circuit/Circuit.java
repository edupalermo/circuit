package circuito.circuit;

import java.util.ArrayList;
import java.util.Iterator;
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
	
	private Set<Port> discarded = new TreeSet<Port>(); 
	
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

	
	
	public void enrich(int initial) {
		
		int actualSize = this.size();
		
		// Adding AND Ports
		for (int i = 0; i < actualSize; i++) {
			for (int j = Math.max(initial, i + 1); j < actualSize; j++ ) {
				this.add(new PortAnd(i, j));
			}
		}
		
		// Adding OR Ports
		for (int i = 0; i < actualSize; i++) {
			for (int j = Math.max(initial, i + 1); j < actualSize; j++ ) {
				this.add(new PortOr(i, j));
			}
		}
		
		// Adding NOT Ports
		for (int i = initial; i < actualSize; i++ ) {
			this.add(new PortNot(i));
		}
		
		// Adding Mem Ports
		for (int i = initial; i < actualSize; i++ ) {
			this.add(new PortMemory(i));
		}
	}
	
	public void simplifyOld(List<Solution> solutions, int oldSize) {
		
		// Assumir que todo mundo eh igual a todo mundo
		
		List<List<Integer>> same = new ArrayList<List<Integer>>();
		for (int i = 0; i < this.size(); i++) {
			List<Integer> list = new ArrayList<Integer>();
			
			for (int j = Math.max((i + 1), oldSize); j < this.size(); j++) {
				list.add(j);
			}
			same.add(list);
		}
		
		for (Solution solution : solutions) {
			simplify(same, solution);
		}
		
		TreeSet<Integer> ordered = new TreeSet<Integer>();
		for (List<Integer> l : same) {
			for (Integer i : l) {
				ordered.add(i);
			}
		}
		
		for (Integer i : ordered.descendingSet()) {
			if (!(this.get(i) instanceof PortInput)) {
				this.remove(i.intValue());
				for (int j = i.intValue(); j < this.size(); j++) {
					this.get(j).adustLeft(i.intValue());
				}
			}
		}
	}

	
	public void simplifySmart(List<Solution> solutions, int oldSize) {
		// Assumir que todo mundo eh igual a todo mundo
		
		Control control = new Control(this.size());
		
		do {
			long initial = System.currentTimeMillis();
		
			List<IntGapList> same = new ArrayList<IntGapList>();
			try {
				for (int i = control.getMin(); i <= control.getMax(); i++) {
					IntGapList list = Application.intGapListPool.borrowObject();
					
					for (int j = 0; j < i; j++) {
						list.add(j);
					}
					same.add(list);
				}
				
				for (Solution solution : solutions) {
					simplify(same, solution, control.getMin());
				}
				
				for (int i = (same.size() - 1); i >= 0; i--) {
					if (same.get(i).size() > 0) {
						int removeIndex = control.getMin() + i; 
						
						if (!(this.get(removeIndex) instanceof PortInput)) {
							this.remove(removeIndex);
							
							for (int j = removeIndex; j < this.size(); j++) {
								this.get(j).adustLeft(removeIndex);
							}
						}
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			finally {
				for (IntGapList intGapList : same) {
					try {
						Application.intGapListPool.returnObject(intGapList);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		
			System.out.println(String.format("Circuit size %d working %d took %d estimative %s", this.size(), control.getDelta(), (System.currentTimeMillis() - initial), control.estimate()));
			
		} while (!control.stop());
	}

	
	public void simplify(List<Solution> solutions, int oldSize) {
		
		long lowestDelta = Integer.MAX_VALUE;
		
		long total = 0;
		long count = 0;
		
		for (int i = this.size() - 1; i >= oldSize; i--) {
			long init = System.currentTimeMillis();
			List<Integer> same = new ArrayList<Integer>();
			
			for (int j = 0; j < i; j++) {
				same.add(j);
			}
			
			for (Solution solution : solutions) {
				simplify(i, same, solution);
			}
			
			if (same.size() > 0) {
				if (!(this.get(i) instanceof PortInput)) {
					this.remove(i);
					long delta = (System.currentTimeMillis() - init);
					lowestDelta = Math.min(lowestDelta, delta);
					count++;
					total += delta;
					System.out.println(String.format("Current size [%d] %d %d ms %3.3f ", this.size(), delta, lowestDelta, ((double) total / (double) count)));
				}
				same.clear();
				same = null;
			}
		}
		
	}

	public void simplify(int index, List<Integer> same, Solution solution) {
		List<Boolean> state = this.generateInitialState();
		
		for (TimeSlice timeSlice : solution.getDialogue()) {
			assignInputToState(state, timeSlice.getInput());
			propagate(state);
			
			Iterator<Integer> it = same.iterator();
			while (it.hasNext()) {
				int j = it.next();
				
				if (state.get(index).booleanValue() != state.get(j).booleanValue()) {
					it.remove();
				}
			}
		}
	}

	
	
	public void simplify(List<List<Integer>> same, Solution solution) {
		List<Boolean> state = this.generateInitialState();
		
		for (TimeSlice timeSlice : solution.getDialogue()) {
			assignInputToState(state, timeSlice.getInput());
			propagate(state);
			
			for (int i = 0; i < same.size(); i++) {
				Iterator<Integer> it = same.get(i).iterator();
				while (it.hasNext()) {
					int j = it.next();
					
					if (state.get(i).booleanValue() != state.get(j).booleanValue()) {
						it.remove();
					}
				}
			}
		}
	}

	
	public void evaluateRepetition(IntGapList same, Solution solution, int index) {
		List<Boolean> state = this.generateInitialState();
		
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

	
	public void simplify(List<IntGapList> same, Solution solution, int shiftMin) {
		List<Boolean> state = this.generateInitialState();
		
		for (TimeSlice timeSlice : solution.getDialogue()) {
			assignInputToState(state, timeSlice.getInput());
			propagate(state);
			
			for (int i = 0; i < same.size(); i++) {
				IntGapList intGapList = same.get(i);
				
				for (int j = (intGapList.size() -1); j >= 0; j--) {
					int z = intGapList.get(j);
					if (state.get(i + shiftMin).booleanValue() != state.get(z).booleanValue()) {
						intGapList.remove(j);
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
	
}
