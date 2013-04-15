package edu.berkeley.cs162;

import junit.framework.TestCase;

public class TestKVCache extends TestCase {

	
	public void testConstructor() {
		//Just check that all entries are invalid after instantiation
	}
	
	//SUPER TEST FUNCTION
	public void testGetPutDel() {	//what about null arguments?
		//WITH AN EMPTY CACHE, all calls to get(key) should return null for any key
		
		//WITH NONEMPTY CACHE
		
	}
	
	
	
	
	public void testGetWriteLock() {
		// idk lol
	}
	
	public void testToXml() {
		//Check the return value of toXML() with an empty cache
		KVStore storeEmpty = new KVStore();
				
		//Check the return value of toXML() with a non-empty cache
		KVStore storeNonEmpty = new KVStore();
		
		//Check the return value of toXML() with a full cache
	}
	
}
