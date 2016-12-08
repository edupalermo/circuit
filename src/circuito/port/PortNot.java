package circuito.port;

import java.util.List;

public class PortNot implements Port {

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
	

}
