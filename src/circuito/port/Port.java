package circuito.port;

import java.io.Serializable;
import java.util.List;

public interface Port extends Serializable {

	boolean evaluate(List<Boolean> list);
	
	void reset();
	
	void adustLeft(int index);

}
