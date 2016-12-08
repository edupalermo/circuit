package circuito.port;

import java.util.List;

public class PortInput implements Port {

	private static final long serialVersionUID = 1L;
	
	private final int index;
	
	public PortInput(int index) {
		this.index = index;
	}
	
	@Override
	public void clear() {};

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortInput) {
			PortInput portInput = (PortInput) obj;
			equals = this.index == portInput.getIndex(); 
		}
		
		return equals;
	}
	
	@Override
	public void adustLeft(int index) {
		throw new RuntimeException("Inconsistency");
	}

	public int getIndex() {
		return index;
	}
	

	public boolean evaluate(List<Boolean> list) {
		return list.get(this.index);
	}
	

}
