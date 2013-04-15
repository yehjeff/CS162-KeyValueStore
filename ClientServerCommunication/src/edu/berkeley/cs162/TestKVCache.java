package edu.berkeley.cs162;

import junit.framework.TestCase;

public class TestKVCache extends TestCase {

	
	public void testConstructor() { 
		//Just check that all entries are invalid after instantiation //HOW TO TEST IF EVERYTHING IS PRIVATE????
		int numSets = 4;
		int maxElemsPerSet = 4;
		KVCache cache = new KVCache(numSets,maxElemsPerSet);
		for (int i = 0; i < numSets; i ++){
			for (int j = 0; j < maxElemsPerSet; j++){
				
				assertTrue(true);
			}
		}

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
		KVCache cacheEmpty = new KVCache(4,4);
				
		//Check the return value of toXML() with a non-empty cache
		KVCache cacheNonEmpty = new KVCache(4,4);
		
		//Check the return value of toXML() with a full cache
		KVCache cacheFull = new KVCache(4,4);

	}
	
}
