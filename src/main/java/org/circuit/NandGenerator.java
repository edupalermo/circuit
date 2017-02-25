package org.circuit;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.circuit.port.Port;
import org.circuit.port.PortNand;

public class NandGenerator {

	private int oldSlice;
	private int size;

	private int left = 0;
	private int right = 1;

	public NandGenerator(int oldSlice, int size) {
		this.oldSlice = oldSlice;
		this.size = size;
		this.right = oldSlice == 0 ? 1 : oldSlice;
	}

	public Port next(int newSize) {
		Port port = null;

		if (left == right) {
			throw new RuntimeException("Inconsistency");
		}

		port = new PortNand(left, right);

		// System.out.println(port);
		inc(newSize);
		return port;
	}

	private void inc(int newSize) {
		if (this.right < (size - 1)) {
			this.right = this.right + 1;
		} else if (this.left < (oldSlice - 1)) {
			this.left = this.left + 1;
			this.right = oldSlice;
		}
		else {

			this.oldSlice = size;
			this.size = size == newSize ? (size + 1) : newSize ;

			this.left = 0;
			this.right = oldSlice;
			
		}
	}

	public static void main(String args[]) throws Exception {

		long total = 2;
		NandGenerator generator = new NandGenerator(0, (int) total);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println(String.format("%d %s ", total, generator.next((int) total)));
			total++;
			//br.readLine();
		}
	}
}