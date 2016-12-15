package org.circuit.list;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.circuit.util.ByteUtils;
import org.circuit.util.IoUtils;
import org.circuit.util.ListUtils;

public class PersistedList<T> implements List<T> {
	
	private final static File folder = new File(".");
	private File file = ListUtils.generateFile(folder);
	
	private RandomAccessFile randomAccessFile = null;


	//All items will have LENGTH and VALUE
	
	private int lengthByteSize = 1;
	private int valueByteSize = 0;
	
	public PersistedList() {
		try {
			randomAccessFile = new RandomAccessFile(ListUtils.generateFile(file), "rw");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean add(T e) {
		if (e == null) {
			throw new RuntimeException("Inconsistency");
		}
		
		byte[] data = IoUtils.objectToBytes(e);
		
		int newDataLengthSize = ByteUtils.bytesNeededToRepresent(data.length);
		
		
		return true;
	}
	

	@Override
	public void add(int index, T element) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public void clear() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean contains(Object o) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public T get(int index) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public int indexOf(Object o) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean isEmpty() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public Iterator<T> iterator() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public ListIterator<T> listIterator() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean remove(Object o) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public T remove(int index) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public T set(int index, T element) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public int size() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public Object[] toArray() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new RuntimeException("Not Implemented");
	}

	
	
	private void recreateFile(int newDataLengthSize, int newDataItselfSize) {
		
		try {
			if (needToRecreateFile(newDataLengthSize, newDataItselfSize)) {
				RandomAccessFile newRandomAccessFile = new RandomAccessFile(ListUtils.generateFile(file), "rw");
				
				for (int i = 0; i < getPhysicalElementsCount(); i++) {
					randomAccessFile.seek(i * getRecordSize());
					
					byte record[] = this.getRecord(i);
					

					
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private byte[] getBytes(long index) {
		int dataLenght = getDataLenght(index);
		return getBytes(index, dataLenght);
	}

	private byte[] getBytes(long index, int dataLength) {
		byte array[] = null;
		try {
			randomAccessFile.seek(index * getRecordSize() + this.lengthByteSize);
			array = new byte[dataLength];
			randomAccessFile.read(array);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return array;
	}

	
	private int getDataLenght(long index) {
		int answer = 0;
		try {
			randomAccessFile.seek(index * getRecordSize());
			byte array[] = new byte[this.lengthByteSize];
			randomAccessFile.read(array);
			answer = ByteUtils.toInt(array);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return answer;
	}

	private byte[] getLengthBytes(long index) {
		byte answer[] = null;
		try {
			randomAccessFile.seek(index * getRecordSize());
			answer = new byte[this.lengthByteSize];
			randomAccessFile.read(answer);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return answer;
	}

	private byte[] getRecord(long index) {
		byte answer[] = null;
		try {
			randomAccessFile.seek(index * getRecordSize());
			int total = this.lengthByteSize + this.valueByteSize;
			int read = 0;
			answer = new byte[total];

			do {
				read += randomAccessFile.read(answer, read, (total - read));
			} while (read < total);


		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return answer;
	}

	private boolean needToRecreateFile(int newDataLengthSize, int newDataItselfSize) {
		return (newDataLengthSize > this.lengthByteSize) || (newDataItselfSize > this.valueByteSize);
	}
	
	private long getPhysicalElementsCount() {
		try {
			return randomAccessFile.length() / (lengthByteSize + valueByteSize);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private long getRecordSize() {
		return this.valueByteSize + this.lengthByteSize;
	}

	
}
