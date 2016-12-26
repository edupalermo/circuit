package org.circuit.list;

import java.util.Arrays;

public class Buffer {
	
	private final static int LIMIT =  10000;
	
	private int indexArray[] = new int[LIMIT];
	private Object objectArray[] = new Object[LIMIT];
	
	private int size = 0;
	
	
	public void add(int index, Object o) {
		if (size < LIMIT) {
			int pos = Arrays.binarySearch(indexArray, 0, size, index);
			if (pos < 0) {
				for (int i = size -1 ; i >= ~pos; i--) {
					indexArray[i+1] = indexArray[i] + 1;
					objectArray[i+1] = objectArray[i]; 
				}
				indexArray[~pos] = index;
				objectArray[~pos] = o;
				
				this.size++;
			}
			else {
				objectArray[pos] = o;
			}
		}
	}
	
	public Object get(int index) {
		Object answer = null;
		int pos = Arrays.binarySearch(indexArray, 0, size, index);
		if (pos > 0) {
			answer = objectArray[pos];
		}
		return answer;
	}
	
	public void remove(int index) {
		int pos = Arrays.binarySearch(indexArray, 0, size, index);
		if (pos > 0) {
			for (int i = pos; i < size - 1; i++) {
				indexArray[i] = indexArray[i + 1] - 1;
				objectArray[i] = objectArray[i + 1];
			}
			size--;
		}
		else {
			for (int i = ~pos; i < size; i++) {
				indexArray[i] = indexArray[i] - 1;
			}
		}
		
	}
	
	public void dump() {
		for (int i =0; i < size; i++) {
			System.out.println(String.format("%d - %s", indexArray[i], objectArray[i].toString()));
		}
	}
	
	public static void main(String[] args) {
		
		Buffer buffer = new Buffer();
		
		
		buffer.add(0, "Eduardo");
		buffer.add(5, "Gomes");
		buffer.add(10, "Palermo");
		
		buffer.dump();
		
		buffer.remove(2);
		
		buffer.dump();
		
		
		buffer.remove(4);
		
		buffer.dump();
		
		buffer.add(1, "Outro Cara");
		
		buffer.dump();
		
		
	}

}
