package org.circuit.port;

import java.io.Serializable;

public interface Port extends Serializable {

	boolean evaluate(boolean list[]);
	
	void reset();
	
	void adustLeft(int index);

}
