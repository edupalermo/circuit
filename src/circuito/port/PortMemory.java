package circuito.port;

import java.util.List;

public class PortMemory implements Port {

	private static final long serialVersionUID = 1L;
	
	private transient boolean memory = false;
	
	private int index;
	
	public PortMemory(int index) {
		this.index = index;
	}

	@Override
	public void clear() {
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
	

}
