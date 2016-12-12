package org.circuit.list;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.circuit.util.IoUtils;
import org.circuit.util.ListUtils;

public class PersistedList<T> implements List<T> {
	
	private final static File folder = new File(".");
	private File file = ListUtils.generateFile(folder);
	
	private RandomAccessFile randomAccessFile = null;
	
	private int dataLengthSize = 1;
	private int dataItselfSize = 0;
	
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
		return true;
	}
	
	private long getPhysicalElementsCount() {
		try {
			return randomAccessFile.length() / (dataLengthSize + dataItselfSize);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

}
