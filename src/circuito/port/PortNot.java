package circuito.port;

import java.util.List;
import java.util.Random;

public class PortNot implements Port, Comparable<Port> {

	private static final long serialVersionUID = 1L;
	
	private int index;
	
	public PortNot(int index) {
		this.index = index;
	}

	@Override
	public void clear() {}
	
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortNot) {
			PortNot portNot = (PortNot) obj;
			equals = this.index == portNot.getIndex(); 
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
		return !list.get(this.index);
	}
	
	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortNot) {
			PortNot portNot = (PortNot) port;
			answer = this.index - portNot.getIndex();
		}
		else {
			answer = this.getClass().getName().compareTo(port.getClass().getName());
		}
		return answer;
	}

	public static PortNot random(int size) {
		Random random = new Random();
		return new PortNot(random.nextInt(size));
	}

	public String toString() {
		return "NOT[" + this.index + "]";
	}

}
