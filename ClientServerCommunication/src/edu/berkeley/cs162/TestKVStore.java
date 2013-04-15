package edu.berkeley.cs162;

import junit.framework.TestCase;

public class TestKVStore extends TestCase {
	
	public void testToXML() { //HOW DO YOU ASSERTEQUAL STUFF WITH SUCH BIG STRINGS LOL
		//Check the return value of toXML() with an empty store
		KVStore storeEmpty = new KVStore();
		System.out.println(storeEmpty.toXML());
		
		//Check the return value of toXML() with a non-empty store
		KVStore storeNonEmpty = new KVStore();
		try{
			storeNonEmpty.put("key1","value1");
			storeNonEmpty.put("key2","value2");
			storeNonEmpty.put("key3","value3");
			storeNonEmpty.put("key4","value4");

		} catch (KVException kve) {
			fail();
		}
		System.out.println(storeNonEmpty.toXML());


	}
	
	public void testDumpToFile() {
		//Check the file output of dumpToFile() with an empty store
		
		//Check the file output of dumpToFile() with a non-empty store
		
	}
	
	public void testRestoreFromFile() {
		//USING toXML() 
		//WITH INITIALLY EMPTY STORE
		//Check store after call to restoreFromFile() with empty xml file
		
		//Check store after call to restoreFromFile() with proper xml file with no key-value pairs
		
		//Check store after call to restoreFromFile() with proper xml file with some key-value pairs 
		
		//Check store after call to restoreFromFile() with an invalid xml file
		
		//WITH INITIALLY NON-EMPTY STORE
		//Check store after call to restoreFromFile() with empty xml file
		//store should just be empty (?????) or exception thrown??
		
		//Check store after call to restoreFromFile() with proper xml file with no key-value pairs
		//store should be empty
		
		//Check store after call to restoreFromFile() with proper xml file with some key-value pairs with keys that DONT match the ones in the store
		//store should just have whats inside xml file
		
		
		//Check store after call to restoreFromFile() with proper xml file with some key-value pairs with keys that DO match the ones in the store
		//store should just have whats inside xml file
		
		
		//Check store after call to restoreFromFile() with an invalid xml file
		//store should just be empty (?????) or exception thrown??
		



	}
	
	
}
