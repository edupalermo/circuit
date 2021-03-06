package org.circuit.circuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.circuit.port.Port;
import org.circuit.port.PortInput;
import org.circuit.solution.TimeSlice;
import org.circuit.util.IoUtils;
import org.circuit.util.RandomUtils;

public class Circuit extends ArrayList<Port> implements Cloneable {

	private static final long serialVersionUID = 1L;

	private boolean checkConsistency = true;
	
	private TreeMap<String, Integer> grades = new TreeMap<String, Integer>();
	
	public static final String GRADE_HIT = "GRADE_HIT";
	public static final String GRADE_CIRCUIT_SIZE = "GRADE_CIRCUIT_SIZE";
	
	private Circuit() {
	}

	public Circuit(int size) {
		for (int i = 0; i < size; i++) {
			this.add(new PortInput(i));
		}
	}

	public Circuit(TimeSlice timeSlice) {
		this(timeSlice.getInput().size());
	}

	
	public void setGrade(String name, Integer grade) {
		if (this.grades == null) {
			this.grades = new TreeMap<String, Integer>();
		}
		this.grades.put(name, grade);
	}

	public Integer getGrade(String name) {
		if (this.grades == null) {
			this.grades = new TreeMap<String, Integer>();
		}
		return this.grades.get(name);
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
	
	private transient String cacheForToString = null;
	private transient int modCountForCache = -1;

	public String toString() {
		
		if ((modCountForCache == this.modCount) && (this.grades.size() > 0) && (cacheForToString != null)) {
			return cacheForToString;
		}
		
		
		StringBuffer sb = new StringBuffer();

		sb.append(this.toSmallString());
		sb.append(" ");
		
		for (int i = 0; i < this.size(); i++) {
			sb.append("[").append(i).append(" ").append(this.get(i).toString()).append("] ");
		}

		sb.deleteCharAt(sb.length() - 1);
		
		
		if (this.grades.size() > 0) {
			this.cacheForToString = sb.toString();
			modCountForCache = this.modCount;
		}


		return sb.toString();
	}

	public String toSmallString() {
		StringBuffer sb = new StringBuffer();
		
		if (this.grades != null) {
			for (Map.Entry<String, Integer> entry : this.grades.entrySet()) {
				sb.append("[");
				sb.append(entry.getKey());
				sb.append("=");
				sb.append(entry.getValue().toString());
				sb.append("] ");
			}
			
			sb.deleteCharAt(sb.length() - 1);
		}
		
		return sb.toString();
	}

	public void reset() {
		for (Port port : this) {
			port.reset();
		}
	}

	public void removePort(int index) {
		
		if (checkConsistency) {
			//logger.info(String.format("Checking [%d] size [%d]", index, size()));
			for (int i = size()-1; i >= index + 1; i--) {
				//logger.info(String.format("Checking [%d] %s", index, get(i).toString()));
				if (get(i).references(index)) {
					//logger.info(String.format("Recurring on [%d]", i));
					throw new RuntimeException("Inconsistency");
				}
			}
		}
		
		for (int i = index + 1; i < size(); i++) {
			this.get(i).adustLeft(index);
		}
		//logger.info("Removing port: " + this.get(index).toString());
		this.remove(index);
	}

	@Override
	public Object clone() {
		Circuit circuit = new Circuit();
		for (Port port : this) {
			circuit.add((Port) port.clone());
		}
		
		return circuit;
	}
	
	private transient String cachedBase64Representation = null;
	
	public String getCachedBase64() {
		if (cachedBase64Representation == null) {
			cachedBase64Representation = IoUtils.objectToBase64(this);
		}
		return cachedBase64Representation;
	}
	
	
}
