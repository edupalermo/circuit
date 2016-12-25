package org.circuit.util;

import java.io.*;

public class IoUtils {

	private static byte BYTE_ZERO = 0x00;

	public static <T> T readObject(File file, Class<T> clazz) {
		T answer = null;

		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(file));
			answer = clazz.cast(ois.readObject());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			closeQuitely(ois);
		}
		return answer;
	}

	public static <T> T writeObject(File file, Object object) {
		T answer = null;

		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			closeQuitely(oos);
		}
		return answer;
	}

	public static byte[] objectToBytes(Object object) {

		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			oos = new ObjectOutputStream(baos = new ByteArrayOutputStream());
			oos.writeObject(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			closeQuitely(oos);
		}

		byte answer[] = null;
		if (baos != null) {
			answer = baos.toByteArray();
		}
		return answer;
	}

	public static Object bytesToObject(byte array[], int offset, int size) {
		Object answer = null;
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(array, offset, size));
			answer = ois.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			closeQuitely(ois);
		}
		return answer;
	}

	public static void closeQuitely(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void closeQuitely(OutputStream os) {
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void fillZeroBytes(RandomAccessFile raf, int actual, int expected) {
		try {
			for (int i = actual; i < expected; i++) {
				raf.write(BYTE_ZERO);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
