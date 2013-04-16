package edu.berkeley.cs162;

import junit.framework.TestCase;

public class TestKVCache extends TestCase {

	
	public void testConstructor() { 
		//Just check that all entries are invalid after instantiation //HOW TO TEST IF EVERYTHING IS PRIVATE????
		

	}
	
	//SUPER TEST FUNCTION
	public void testGetPutDel() {	//what about null arguments?
		int numSets = 4;
		int maxElemsPerSet = 4;
		KVCache cache = new KVCache(numSets,maxElemsPerSet);
		
		//all calls to get should return null when nothing is inside the cache
		String getReturnValue;
		getReturnValue = cache.get("key1");
		assertTrue(getReturnValue == null);
		getReturnValue = cache.get("key2");
		assertTrue(getReturnValue == null);
		getReturnValue = cache.get("key3");
		assertTrue(getReturnValue == null);
		System.out.println(cache.toXML());
		
		//using get to ensure put was successful
		cache.put("key1", "value1");
		getReturnValue = cache.get("key1");
		assertTrue(getReturnValue != null);
		assertTrue(getReturnValue.equals("value1"));
		
		//and get should still not work for other keys that were not put'd
		getReturnValue = cache.get("key2");
		assertTrue(getReturnValue == null);
		
		//trying multiple puts with different keys
		cache.put("key2", "value2");
		getReturnValue = cache.get("key1");
		assertTrue(getReturnValue != null);
		assertTrue(getReturnValue.equals("value1"));
		getReturnValue = cache.get("key2");
		assertTrue(getReturnValue != null);
		assertTrue(getReturnValue.equals("value2"));
		
		//using put to overwrite a previous put
		cache.put("key1","altvalue1");
		getReturnValue = cache.get("key1");
		assertTrue(getReturnValue != null);
		assertTrue(getReturnValue.equals("altvalue1"));
		
		//now using delete to ensure a kv pair gets deleted
		cache.del("key1");
		getReturnValue = cache.get("key1");
		assertTrue(getReturnValue == null);

		
		
	}
	
	public void testEvictionPolicy(){
		//to ensure kv pairs are in the same set, have a single-set cache
		int numSets = 1;
		int maxElemsPerSet = 4;
		KVCache cache = new KVCache(numSets, maxElemsPerSet);
		String getReturn;
		//fill up the set first
		cache.put("key1", "value1");
		cache.put("key2", "value2");
		cache.put("key3", "value3");
		cache.put("key4", "value4");
		
		//add a new kvpair, key5, then key1 should be replaced, since it was the first one to be added
		cache.put("key5", "value5");
		getReturn = cache.get("key1");
		assertTrue(getReturn == null);
		getReturn = cache.get("key2");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value2"));
		getReturn = cache.get("key3");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value3"));
		getReturn = cache.get("key4");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value4"));
		getReturn = cache.get("key5");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value5"));
		
		//now key2 should be replaced
		cache.put("key6","value6");
		getReturn = cache.get("key1");
		assertTrue(getReturn == null);
		getReturn = cache.get("key2");
		assertTrue(getReturn == null);
		getReturn = cache.get("key3");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value3"));
		getReturn = cache.get("key4");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value4"));
		getReturn = cache.get("key5");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value5"));
		getReturn = cache.get("key6");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value6"));
		
		
		
		
		
		
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
