package org.circuit.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestByteUtils {
	
	
	@DataProvider
	public Object[][] dataProvider() {
		return new Object[][] {
				{0 ,1},	
				{1 ,1},	
				{10 ,1},	
				{100 ,1},	
				{255 ,1},	
				{256 ,2},	
				{257 ,2},	
				{65535 ,2},	
				{65536 ,3},	
				{16777215 ,3},	
				{16777216 ,4}	
		};
	}
	
	@Test(dataProvider="dataProvider")
	public void testBytesNeeded(Integer number, Integer bytes) {
		assertThat(ByteUtils.bytesNeededToRepresent(number.intValue()), equalTo(bytes.intValue()));
		
		
	}
	
	

}
