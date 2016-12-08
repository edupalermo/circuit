package circuito.port;

import java.util.List;
import java.util.Random;

public class PortMemory implements Port, Comparable<Port> {

	private static final long serialVersionUID = 1L;
	
	private transient boolean memory = false;
	
	private int index;
	
	public PortMemory(int index) {
		this.index = index;
	}

	@Override
	public void reset() {
		this.memory = false;
	}

	
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortMemory) {
			PortMemory portMemory = (PortMemory) obj;
			equals = this.index == portMemory.getIndex(); 
		}
		
		return equals;
	}
	
	@Override
	public void adustLeft(int index) {
		if (this.index > index) {
			this.index--;
		}
	}

	public int getIndex() {
		return index;
	}
	
	public boolean evaluate(List<Boolean> list) {
		boolean answer = this.memory;
		this.memory = list.get(this.index);
		return answer;
	}
	
	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortMemory) {
			PortMemory portMemory = (PortMemory) port;
			answer = this.index - portMemory.getIndex();
		}
		else {
			answer = this.getClass().getName().compareTo(port.getClass().getName());
		}
		return answer;
	}

	public static PortMemory random(int size) {
		Random random = new Random();
		return new PortMemory(random.nextInt(size));
	}

	public String toString() {
		return "MEM[" + this.index + "]";
	}
}
