package circuito.port;

import java.util.List;
import java.util.Random;

public class PortOr implements Port, Comparable<Port> {

	private static final long serialVersionUID = 1L;
	
	private int minor;
	private int major;
	
	public PortOr(int left, int right) {
		this.minor = Math.min(left, right);
		this.major = Math.max(left, right);
	}

	@Override
	public void reset() {}
	
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
	
	public boolean evaluate(boolean list[]) {
		return list[this.minor] || list[this.major];
	}
	
	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortOr) {
			PortOr portOr = (PortOr) port;
			answer = this.minor - portOr.getMinor();
			if (answer == 0) {
				answer = this.major - portOr.getMajor();
			}
		}
		else {
			answer = this.getClass().getName().compareTo(port.getClass().getName());
		}
		return answer;
	}
	
	public static PortOr random(int size) {
		Random random = new Random();
		int l = random.nextInt(size);
		int r = 0;
		while ((r = random.nextInt(size)) == l);
		return new PortOr(l, r);
	}

	public String toString() {
		return "OR[" + this.minor + "," + this.major + "]";
	}
}
