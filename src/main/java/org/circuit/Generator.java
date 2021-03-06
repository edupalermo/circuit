package org.circuit;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.circuit.port.Port;
import org.circuit.port.PortAnd;
import org.circuit.port.PortMemory;
import org.circuit.port.PortNot;
import org.circuit.port.PortOr;

public class Generator {
	
	private int oldSlice;
	private int size;

	private int portType = 0;
	private int left = 0;
	private int right = 1;
	
	public Generator(int oldSlice, int size) {
		this.oldSlice = oldSlice;
		this.size = size;
		this.right = oldSlice;
	}
	
	public Port next(int newSize) {
		Port port = null;
		
		if (((portType == 0) || (portType == 1)) && (left == right)) {
			inc(newSize);
		}
		
		switch(portType) {
				case 0:
					port = new PortAnd(left , right);
					break;
				case 1:
					port = new PortOr(left , right);
					break;
				case 2: 
					port = new PortNot(right);
					break;
				case 3:
					port = new PortMemory(right);
					break;
				default:
					throw new RuntimeException("Inconsistency");
		}
		//System.out.println(port);
		inc(newSize);
		return port;
	}
	
	private void inc(int newSize) {
	switch(portType) {
		case 0:
		case 1:
			if (this.right < (size - 1)) {
				this.right = this.right + 1; 
			}
			else if (this.left < (oldSlice - 2)){
				this.left = this.left + 1;
				this.right = oldSlice;
			}
			else {
				portType++;
				this.left = 0;
				this.right = oldSlice;
			}
			break;
		case 2: 
		case 3:
			if (this.right < (size - 1)) {
				this.right++; 
			}
			else if (portType < 3) {
				portType++;
				this.left = 0;
				this.right = oldSlice;
			}
			else {
				portType = 0;
				this.left = 0;
				this.right = size;
				this.oldSlice = size;
				this.size = newSize;
			}
			break;
		default:
			throw new RuntimeException("Inconsistency");
		}
		//System.out.println(String.format("Size %d OldSlice %d Type %d Left %d Right %d ", size, oldSlice, portType, left, right));
	}
	
	
	public static void main(String args[]) throws Exception {
		
		long total = 2;
		Generator generator = new Generator(0, (int) total);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println(String.format("%d %s ", total, generator.next((int) total)));
			total++;
			br.readLine();
		}
	}
}