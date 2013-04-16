package edu.berkeley.cs162;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class TestKVCache extends TestCase {

	
	public void testConstructor() { 
		//Just check that all entries are invalid after instantiation //HOW TO TEST IF EVERYTHING IS PRIVATE????
		

	}
	
	//SUPER TEST FUNCTION
	public void testGetPutDel() {	//what about null arguments?
		KVCache cache = new KVCache(4,4);
		
		//all calls to get should return null when nothing is inside the cache
		String getReturnValue;
		getReturnValue = cache.get("key1");
		assertTrue(getReturnValue == null);
		getReturnValue = cache.get("key2");
		assertTrue(getReturnValue == null);
		getReturnValue = cache.get("key3");
		assertTrue(getReturnValue == null);
		
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
		KVCache cache = new KVCache(1, 4);
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
	
	public void testToXml() throws Exception {
		
		//Check the return value of toXML() with an empty cache, use parser for comparison
		// can also check key and values (all should be empty)
		int numSets = 4;
		int maxElemsPerSet = 4;
		KVCache emptyCache = new KVCache(numSets, maxElemsPerSet);
		String XMLString = emptyCache.toXML();
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(XMLString)));

		NodeList setList = doc.getElementsByTagName("Set");
		assertTrue(setList.getLength() == numSets);
		NodeList cacheEntryList = doc.getElementsByTagName("CacheEntry");
		assertTrue(cacheEntryList.getLength() == numSets*maxElemsPerSet);
		for (int cacheEntryIndex = 0; cacheEntryIndex < cacheEntryList.getLength(); cacheEntryIndex++){
			Element cacheEntryElem = (Element) cacheEntryList.item(cacheEntryIndex);
			String isReferenced = cacheEntryElem.getAttribute("isReferenced");
			String isValid = cacheEntryElem.getAttribute("isValid");
			assertTrue(isReferenced.equals("false"));
			assertTrue(isValid.equals("false"));
		}
		
		
		
		
		
		
		
		

		
		//Check the return value of toXML() with a non-empty cache
		numSets = 4;
		maxElemsPerSet = 4;
		KVCache nonemptyCache = new KVCache(numSets, maxElemsPerSet);
		nonemptyCache.put("key1", "value1");
		XMLString = nonemptyCache.toXML();
		
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = builder.parse(new InputSource(new StringReader(XMLString)));

		setList = doc.getElementsByTagName("Set");
		assertTrue(setList.getLength() == numSets);
		cacheEntryList = doc.getElementsByTagName("CacheEntry");
		assertTrue(cacheEntryList.getLength() == numSets*maxElemsPerSet);
		for (int cacheEntryIndex = 0; cacheEntryIndex < cacheEntryList.getLength(); cacheEntryIndex++){
			Element cacheEntryElem = (Element) cacheEntryList.item(cacheEntryIndex);
			String isValid = cacheEntryElem.getAttribute("isValid");
			if (isValid.equals("true")){
				assertTrue(cacheEntryElem.getElementsByTagName("Key").item(0).getTextContent().equals("key1"));
				assertTrue(cacheEntryElem.getElementsByTagName("Value").item(0).getTextContent().equals("value1"));
			}
		}
		//Check the return value of toXML() with a full cache, all isValid attributes should be true
		numSets = 1;
		maxElemsPerSet = 4;
		KVCache fullCache = new KVCache(numSets,maxElemsPerSet);
		fullCache.put("key1", "value1");
		fullCache.put("key2", "value2");
		fullCache.put("key3", "value3");
		fullCache.put("key4", "value4");
		XMLString = fullCache.toXML();
		System.out.println(fullCache.toXML());

		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = builder.parse(new InputSource(new StringReader(XMLString)));

		setList = doc.getElementsByTagName("Set");
		assertTrue(setList.getLength() == numSets);
		cacheEntryList = doc.getElementsByTagName("CacheEntry");
		assertTrue(cacheEntryList.getLength() == numSets*maxElemsPerSet);
		for (int cacheEntryIndex = 0; cacheEntryIndex < cacheEntryList.getLength(); cacheEntryIndex++){
			Element cacheEntryElem = (Element) cacheEntryList.item(cacheEntryIndex);
			String isValid = cacheEntryElem.getAttribute("isValid");
			assertTrue(isValid.equals("true"));

		}

	}
	
}
