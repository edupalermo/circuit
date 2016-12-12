package org.circuit.util;

import java.io.File;
import java.util.Random;

public class ListUtils {

	public static File generateFile(File dir) {
		File file = null;
		
		do {
			file = new File(dir, generateFilename());
		} while (file.exists());
		
		return file;
	}
	
	public static String generateFilename() {
		
		StringBuffer sb = new StringBuffer();
		
		Random random = new Random();
		
		sb.append("list_");
		for (int i = 0; i < 10; i++) {
			sb.append('a' + (char)random.nextInt('z' - 'a'));
		}
		sb.append(".dat");
		
		return sb.toString();
	}
	
}
