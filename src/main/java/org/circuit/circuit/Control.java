package org.circuit.circuit;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Control {
	
	private int min;
	private int max;
	
	private int size;
	
	private long tick = System.currentTimeMillis();
	
	private final static long DELTA_MAX = 15000;
	
	public Control(int size) {
		this.max = size - 1;
		this.min = size - 2;
		
		this.size = size;
	}

	public int getMin() {
		return min;
	}
	

	public int getMax() {
		return max;
	}
	
	public int getDelta() {
		return this.max - this.min;
	}
	
	public boolean stop() {
		boolean stop = false;
		if (this.min == 0) {
			stop = true;
		}
		else {
			int range = this.max - this.min;
			long delta = System.currentTimeMillis() - this.tick;
			
			range = Math.min(Math.max((int)(((double)(DELTA_MAX * range)) / ((double)delta)), 1), 1000);
			
			this.max = this.min - 1;
			this.min = Math.max(this.max - range, 0);
			
			this.tick = System.currentTimeMillis();
		}
		return stop;
	}
	
	public String estimate() {
		double delta = this.max - this.min;
		
		int remaningSeconds = (int)(((double)(DELTA_MAX * this.min)) / delta);
		
		LocalDateTime ldt = LocalDateTime.now();
		ldt = ldt.plusSeconds(remaningSeconds);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		
		return formatter.print(ldt);
	}

}
