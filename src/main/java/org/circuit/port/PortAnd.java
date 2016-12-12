package org.circuit.port;

import java.util.List;
import java.util.Random;

public class PortAnd implements Port, Comparable<Port> {

	private static final long serialVersionUID = 1L;
	
	private int minor;
	private int major;
	
	public PortAnd(int left, int right) {
		this.minor = Math.min(left, right);
		this.major = Math.max(left, right);
	}
	
	@Override
	public void reset() {};

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		
		if (obj instanceof PortAnd) {
			PortAnd portAnd = (PortAnd) obj;
			equals = (this.minor == portAnd.getMinor()) && (this.major == portAnd.getMajor()); 
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
		return list[this.minor] && list[this.major];
	}

	@Override
	public int compareTo(Port port) {
		int answer = 0;
		if (port instanceof PortAnd) {
			PortAnd portAnd = (PortAnd) port;
			answer = this.minor - portAnd.getMinor();
			if (answer == 0) {
				answer = this.major - portAnd.getMajor();
			}
		}
		else {
			answer = this.getClass().getName().compareTo(port.getClass().getName());
		}
		return answer;
	}
	
	public static PortAnd random(int size) {
		Random random = new Random();
		int l = random.nextInt(size);
		int r = 0;
		while ((r = random.nextInt(size)) == l);
		return new PortAnd(l, r);
	}

	public String toString() {
		return "AND[" + this.minor + "," + this.major + "]";
	}

}
