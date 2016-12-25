package org.circuit.list;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import org.circuit.util.ByteUtils;
import org.circuit.util.IoUtils;
import org.circuit.util.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistedList<E> implements List<E> {
	
	private final transient static Logger logger = LoggerFactory.getLogger(PersistedList.class);

	private final static File folder = new File("c:\\temp\\list");
	private File file = this.generateTemporaryFile();

	private RandomAccessFile randomAccessFile = null;

	private static int GAP_LIMIT = 100000;
	
	private Set<Long> removedPhysicaIndices = new TreeSet<Long>();

	// Length And Value

	private int lengthSize = 0;
	private int valueSize = 0;
	
	private long modCount = 0;
	
	private int size = 0;

	public PersistedList() {
		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean add(E element) {
		try {
			if (element == null) {
				throw new RuntimeException("Inconsistency");
			}

			byte valueData[] = IoUtils.objectToBytes(element);
			
			int newLengthSize = ByteUtils.bytesNeededToRepresent(valueData.length);
			
			if (needToRecreateFile(newLengthSize, valueData.length)) {
				this.recreateFile(newLengthSize, valueData.length);
			}
			
			randomAccessFile.seek(randomAccessFile.length());
			
			byte lengthData[] = ByteUtils.toByteArray(valueData.length);
			
			IoUtils.fillZeroBytes(randomAccessFile, lengthData.length, this.lengthSize);
			randomAccessFile.write(lengthData);
			randomAccessFile.write(valueData);
			IoUtils.fillZeroBytes(randomAccessFile, valueData.length, this.valueSize);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.size++;
		
		this.modCount++;
		
//		checkConsistency();
		return true;
	}
	
	@Override
	public E get(int index) {
		return this.getByPhysicalIndex(this.getPhysicalIndex(index));
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public Iterator<E> iterator() {
		return new PersistedIterator<E>(this);
	}

	@Override
	public E remove(int index) {
		if ((index < 0) || (index >= size)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		long physicalIndex = this.getPhysicalIndex(index);
		E e = this.getByPhysicalIndex(physicalIndex);
		this.removedPhysicaIndices.add(physicalIndex);

		this.modCount++;
		this.size--;
		
		if (this.removedPhysicaIndices.size() >= GAP_LIMIT) {
			this.recreateFile(this.lengthSize, this.valueSize);
		}
		
		//checkConsistency();
		
		return e;
	}

	

	
	
	
	
	
	
	
	
	
	
	private void checkConsistency() {
		
		if (getRecordSize() != 0) {
			try {
				logger.info(String.format("%d %d", (this.randomAccessFile.length() /getRecordSize()), (this.size + this.removedPhysicaIndices.size())));
				if ((this.randomAccessFile.length() /getRecordSize()) != (this.size + this.removedPhysicaIndices.size())) {
					throw new RuntimeException("Inconsistency!");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		}
		
	}
	
	
	public E getByPhysicalIndex(long physicalIndex) {
		byte record[] = this.getRecord(physicalIndex);
		int currentValueLength = ByteUtils.toInt(record, 0, this.lengthSize);
		
		return (E) IoUtils.bytesToObject(record, this.lengthSize, currentValueLength);
	}
	
	
	private long getPhysicalIndex(long index){
		
		long physicalIndex = index;
		
		long j = 0;
		
		Iterator<Long> it = this.removedPhysicaIndices.iterator();
		
		while (it.hasNext() && (it.next().longValue() <= physicalIndex)) {
			physicalIndex++;
		}
		
		return physicalIndex;
		
	}

	
	
	protected void recreateFile() {
		this.recreateFile(this.lengthSize, this.valueSize);
	}

	private void recreateFile(int newLengthSize, int newValueSize) {
		
		//checkConsistency();

		try {
			System.out.println("Recreating file.");
			File newFile = this.generateTemporaryFile();
			RandomAccessFile newRandomAccessFile = new RandomAccessFile(newFile, "rw");

			for (int i = 0; i < getPhysicalElementsCount(); i++) {
				
				if (this.removedPhysicaIndices.contains(Long.valueOf(i))) {
					continue;
				}

				byte data[] = getRecord(i);

				if ((this.lengthSize < newLengthSize) && (this.valueSize >= newValueSize)) {
					IoUtils.fillZeroBytes(newRandomAccessFile, this.lengthSize, newLengthSize);
					newRandomAccessFile.write(data);
				} else if ((this.lengthSize >= newLengthSize) && (this.valueSize < newValueSize)) {
					newRandomAccessFile.write(data);
					IoUtils.fillZeroBytes(newRandomAccessFile, this.valueSize, newValueSize);
				} else if ((this.lengthSize < newLengthSize) && (this.valueSize < newValueSize)) {
					IoUtils.fillZeroBytes(newRandomAccessFile, this.lengthSize, newLengthSize);
					newRandomAccessFile.write(data);
					IoUtils.fillZeroBytes(newRandomAccessFile, this.valueSize, newValueSize);
				} else {
					newRandomAccessFile.write(data);
				}
			}
			
			this.removedPhysicaIndices.clear();
			
			this.randomAccessFile.close();
			this.randomAccessFile = newRandomAccessFile;
			
			if (!this.file.delete()) {
				System.err.println(String.format("Fail to delete file %s", this.file.getAbsoluteFile()));
			}
			this.file = newFile;
			
			this.lengthSize = newLengthSize;
			this.valueSize = newValueSize;
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
	
	private File generateTemporaryFile() {
		File file = ListUtils.generateFile(folder);
		file.deleteOnExit();
		System.out.println(file.getAbsolutePath());
		return file;
	}

	private byte[] getRecord(long physicaIndex) {
		byte array[] = null;
		try {
			int recordSize = getRecordSize();
			randomAccessFile.seek(physicaIndex * recordSize);
			array = new byte[recordSize];
			randomAccessFile.readFully(array);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return array;
	}

	private boolean needToRecreateFile(int newDataLengthSize, int newDataItselfSize) {
		return (newDataLengthSize > this.lengthSize) || (newDataItselfSize > this.valueSize);
	}

	private long getPhysicalElementsCount() {
		return this.size + this.removedPhysicaIndices.size();
	}

	private int getRecordSize() {
		return this.valueSize + this.lengthSize;
	}
	
	protected long getModCount() {
		return this.modCount;
	}
	
	@Override
	protected void finalize() throws Throwable {
		System.out.println("Calling finalize...");
		this.randomAccessFile.close();
		if (this.file.exists()) {
			this.file.delete();
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void add(int index, E element) {
		throw new RuntimeException("Not Implemented");
		
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
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
	public int indexOf(Object o) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean isEmpty() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public ListIterator<E> listIterator() {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public boolean remove(Object o) {
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
	public E set(int index, E element) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
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

	
	

	
	
	
	
	
	
	
	public static void main (String args[]) {
		
		PersistedList<String> persistedList = new PersistedList<String>();
		
		persistedList.add("Eduardo");
		persistedList.add("Gomes");
		persistedList.add("Eduardo Gomes Palermo");
		
		//for (int i = 0; i< 256; i++) {
		//	persistedList.add(Integer.toString(i));
		//}
		persistedList.add("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		
		persistedList.remove(0);
		persistedList.remove(0);
		persistedList.remove(1);
		
		persistedList.add("Lasst");

		persistedList.remove(1);
		persistedList.add("Last one");
		persistedList.add("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		
		while (persistedList.size() > 0) {
			persistedList.remove(persistedList.size() - 1);
		}
		
		persistedList.add("0");
		persistedList.add("1");
		persistedList.add("2");
		
		persistedList.remove(1);
		
		persistedList.add("4");
		persistedList.add("6");

		persistedList.remove(0);
		persistedList.remove(1);
		
		persistedList.recreateFile();
		
		for (int i = 0; i < persistedList.size(); i++) {
			System.out.println(i + " - " + persistedList.get(i));
		}
	}
	
}
