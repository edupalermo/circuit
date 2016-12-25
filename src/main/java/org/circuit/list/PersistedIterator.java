package org.circuit.list;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class PersistedIterator<E> implements Iterator<E> {
	
	private PersistedList<E> persistedList;
	
	private long cursor = 0;
	
	private long expectedMoCount;
	
	public PersistedIterator(PersistedList<E> persistedList) {
		this.persistedList = persistedList;
		
		this.expectedMoCount = this.persistedList.getModCount();
	}

	@Override
	public boolean hasNext() {
		this.checkForComodificatgion();
		return this.cursor < (this.persistedList.size() - 1);
	}

	@Override
	public E next() {
		this.checkForComodificatgion();
		if ((this.cursor < 0) || (this.cursor >= this.persistedList.size())) {
			throw new NoSuchElementException();
		}
		int actual = (int)this.cursor;
		this.cursor ++;
		return this.persistedList.get(actual);
	}
	
	private void checkForComodificatgion() {
		if (this.expectedMoCount != this.persistedList.getModCount()) {
			throw new ConcurrentModificationException();
		}
	}
	

}
