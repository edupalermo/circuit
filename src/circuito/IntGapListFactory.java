package circuito;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.magicwerk.brownies.collections.primitive.IntGapList;

public class IntGapListFactory extends BasePooledObjectFactory<IntGapList> {

	@Override
	public IntGapList create() throws Exception {
		return new IntGapList();
	}

	@Override
	public PooledObject<IntGapList> wrap(IntGapList buffer) {
		return new DefaultPooledObject<IntGapList>(buffer);
	}
	

    /**
     * When an object is returned to the pool, clear the buffer.
     */
    @Override
    public void passivateObject(PooledObject<IntGapList> pooledObject) {
    	pooledObject.getObject().clear();
    }

}
