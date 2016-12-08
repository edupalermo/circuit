package circuito.port;

import java.util.List;

public class PortOr implements Port {

	private static final long serialVersionUID = 1L;
	
	private int minor;
	private int major;
	
	public PortOr(int left, int right) {
		this.minor = Math.min(left, right);
		this.major = Math.max(left, right);
	}

	@Override
	public void clear() {}
	
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortOr) {
			PortOr portOr = (PortOr) obj;
			equals = (this.minor == portOr.getMinor()) && (this.major == portOr.getMajor()); 
		}
		
		return equals;
	}
	
	@Override
	public void adustLeft(int index) {
		if (minor > index) {
			this.minor--;
		}
		
		if (major > index) {
			this.major--;
		}
	}

	public int getMinor() {
		return minor;
	}
	

	public int getMajor() {
		return major;
	}
	
	public boolean evaluate(List<Boolean> list) {
		return list.get(this.minor) || list.get(this.major);
	}
	

}
