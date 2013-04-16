package edu.berkeley.cs162;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class TestKVStore extends TestCase {
	
	public void testToXML() throws Exception { //HOW DO YOU ASSERTEQUAL STUFF WITH SUCH BIG STRINGS LOL
		//Check the return value of toXML() with an empty store
		KVStore emptyStore = new KVStore();
		String XMLString = emptyStore.toXML();
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(XMLString)));

		NodeList KVPairList = doc.getElementsByTagName("KVPair");
		assertTrue(KVPairList.getLength() == 0);
		
		
		
		
		
		
		
		
		//Check the return value of toXML() with a non-empty store
		KVStore nonemptyStore = new KVStore();
		nonemptyStore.put("key1", "value1");
		nonemptyStore.put("key2", "value2");
		nonemptyStore.put("key3", "value3");
		nonemptyStore.put("key4", "value4");
		nonemptyStore.put("key5", "value5");
		nonemptyStore.put("key6", "value6");
		XMLString = nonemptyStore.toXML();
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = builder.parse(new InputSource(new StringReader(XMLString)));
		KVPairList = doc.getElementsByTagName("KVPair");
		boolean checkoffList[] = new boolean[6];
		for (int i = 0; i < KVPairList.getLength(); i++){
			Element KVPairElement = (Element) KVPairList.item(i);
			String key = KVPairElement.getElementsByTagName("Key").item(0).getTextContent();
			String value = KVPairElement.getElementsByTagName("Value").item(0).getTextContent();

			if (key.equals("key1")) {
				if (value.equals("value1"))
					checkoffList[0] = true;
			} else if (key.equals("key2")) {
				if (value.equals("value2"))
					checkoffList[1] = true;
			} else if (key.equals("key3")) {
				if (value.equals("value3"))
					checkoffList[2] = true;
			} else if (key.equals("key4")) {
				if (value.equals("value4"))
					checkoffList[3] = true;
			} else if (key.equals("key5")) {
				if (value.equals("value5"))
					checkoffList[4] = true;
			} else if (key.equals("key6")) {
				if (value.equals("value6"))
					checkoffList[5] = true;
			}
		}

		for (int i = 0; i < checkoffList.length; i++){
			if (!checkoffList[i])
				fail();
		}


	}
	
	public void testDumpToFile() throws Exception {
		//Check the file output of dumpToFile() with an empty store; dont forget to remove file
		
		String dumpXMLFilename = "emptyStoreTestDump.xml";
		KVStore emptyStore = new KVStore();
		emptyStore.dumpToFile(dumpXMLFilename);
		emptyStore = new KVStore();
		emptyStore.restoreFromFile(dumpXMLFilename);
		
		File xmlFile = new File(dumpXMLFilename);
		DocumentBuilder builder;
		Document doc;
		NodeList KVPairList;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(xmlFile);
			KVPairList = doc.getElementsByTagName("KVPair");
			assertTrue(KVPairList.getLength() == 0);
		} finally {
			xmlFile.delete();
		}
		
		
		
		
		
		
		
		
		//Check the file output of dumpToFile() with a non-empty store; dont forget to remove file

		dumpXMLFilename = "nonemptyStoreTestDump.xml";
		xmlFile = new File(dumpXMLFilename);
		try {
			KVStore nonemptyStore = new KVStore();
			nonemptyStore.put("key1", "value1");
			nonemptyStore.put("key2", "value2");
			nonemptyStore.put("key3", "value3");
			nonemptyStore.put("key4", "value4");
			nonemptyStore.put("key5", "value5");
			nonemptyStore.put("key6", "value6");
			nonemptyStore.dumpToFile(dumpXMLFilename);

			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(xmlFile);
			KVPairList = doc.getElementsByTagName("KVPair");
			boolean checkoffList[] = new boolean[6];
			for (int i = 0; i < KVPairList.getLength(); i++){
				Element KVPairElement = (Element) KVPairList.item(i);
				String key = KVPairElement.getElementsByTagName("Key").item(0).getTextContent();
				String value = KVPairElement.getElementsByTagName("Value").item(0).getTextContent();

				if (key.equals("key1")) {
					if (value.equals("value1"))
						checkoffList[0] = true;
				} else if (key.equals("key2")) {
					if (value.equals("value2"))
						checkoffList[1] = true;
				} else if (key.equals("key3")) {
					if (value.equals("value3"))
						checkoffList[2] = true;
				} else if (key.equals("key4")) {
					if (value.equals("value4"))
						checkoffList[3] = true;
				} else if (key.equals("key5")) {
					if (value.equals("value5"))
						checkoffList[4] = true;
				} else if (key.equals("key6")) {
					if (value.equals("value6"))
						checkoffList[5] = true;
				}
			}

			for (int i = 0; i < checkoffList.length; i++){
				if (!checkoffList[i])
					fail();
			}
		} finally {
			xmlFile.delete();
		}
	}

	public void testRestoreFromFile() throws Exception{	//TODO: Delete xml files after done with test
		//USING toXML() 
		//WITH INITIALLY EMPTY STORE
		//Check store after call to restoreFromFile() with empty xml file
		String XMLFilename = "emptyFile.xml";
		BufferedWriter writer = new BufferedWriter(new FileWriter(XMLFilename));
		writer.write("");
		writer.close();
		
		KVStore emptyStore = new KVStore();
		emptyStore.restoreFromFile(XMLFilename);		// should just not crash and print out "unable to read xml file"
		
		
		
		
		
		
		
		//Check store after call to restoreFromFile() with proper xml file with no key-value pairs
		XMLFilename = "noKVPairFile.xml";
		writer = new BufferedWriter(new FileWriter(XMLFilename));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><KVStore/>");
		writer.close();
		
		emptyStore = new KVStore();
		emptyStore.restoreFromFile(XMLFilename);	// should work, just store should be empty (HOW TO CHECK THIS?)
		
		
		
		
		
		
		
		
		
		//Check store after call to restoreFromFile() with proper xml file with some key-value pairs 
		XMLFilename = "someKVPairFile.xml";
		String getReturn;
		writer = new BufferedWriter(new FileWriter(XMLFilename));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
				"<KVStore>" +
				"<KVPair><Key>key1</Key><Value>value1</Value></KVPair>" +
				"<KVPair><Key>key2</Key><Value>value2</Value></KVPair>" +
				"<KVPair><Key>key3</Key><Value>value3</Value></KVPair>" +
				"<KVPair><Key>key4</Key><Value>value4</Value></KVPair>" +
				"<KVPair><Key>key5</Key><Value>value5</Value></KVPair>" +
				"<KVPair><Key>key6</Key><Value>value6</Value></KVPair>" +
				"</KVStore>");
		writer.close();
		
		emptyStore = new KVStore();
		emptyStore.restoreFromFile(XMLFilename);	// should work, just store should be empty (HOW TO CHECK THIS?)
		getReturn = emptyStore.get("key1");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value1"));
		getReturn = emptyStore.get("key2");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value2"));
		getReturn = emptyStore.get("key3");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value3"));
		getReturn = emptyStore.get("key4");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value4"));
		getReturn = emptyStore.get("key5");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value5"));
		getReturn = emptyStore.get("key6");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value6"));
		try {
			getReturn = emptyStore.get("key7");
			fail();
		} catch (KVException e) {
			// PASS (HOW DO YOU PASS?)
		}
		
		
		
		
		
		
		
		
		
		
		//Check store after call to restoreFromFile() with an invalid xml file
		
		//WITH INITIALLY NON-EMPTY STORE
		//Check store after call to restoreFromFile() with empty xml file
		//store should just be empty (?????) or exception thrown??
		KVStore nonemptyStore = new KVStore();
		
		
		
		
		
		
		
		
		
		
		
		
		
		//Check store after call to restoreFromFile() with proper xml file with no key-value pairs
		//store should be empty
		
		
		XMLFilename = "noKVPairFile.xml";
		writer = new BufferedWriter(new FileWriter(XMLFilename));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><KVStore/>");
		writer.close();
		
		nonemptyStore = new KVStore();
		nonemptyStore.put("key1", "value1");
		nonemptyStore.put("key2", "value2");
		nonemptyStore.put("key3", "value3");
		nonemptyStore.restoreFromFile(XMLFilename);	// should work, store should be empty 
		try {
			getReturn = nonemptyStore.get("key1");
			System.out.println(getReturn);
			fail();
		} catch (KVException e) {
			// PASS (HOW DO YOU PASS?)
		}
		try {
			getReturn = nonemptyStore.get("key2");
			fail();
		} catch (KVException e) {
			// PASS (HOW DO YOU PASS?)
		}
		try {
			getReturn = nonemptyStore.get("key3");
			fail();
		} catch (KVException e) {
			// PASS (HOW DO YOU PASS?)
		}
		
		
		
		
		
		//Check store after call to restoreFromFile() with proper xml file with some key-value pairs with keys that DONT match the ones in the store
		//store should just have whats inside xml file
		XMLFilename = "someKVPairFile.xml";
		writer = new BufferedWriter(new FileWriter(XMLFilename));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
				"<KVStore>" +
				"<KVPair><Key>key4</Key><Value>value4</Value></KVPair>" +
				"<KVPair><Key>key5</Key><Value>value5</Value></KVPair>" +
				"<KVPair><Key>key6</Key><Value>value6</Value></KVPair>" +
				"<KVPair><Key>key7</Key><Value>value7</Value></KVPair>" +
				"<KVPair><Key>key8</Key><Value>value8</Value></KVPair>" +
				"<KVPair><Key>key9</Key><Value>value9</Value></KVPair>" +
				"</KVStore>");		
		writer.close();
		
		nonemptyStore = new KVStore();
		nonemptyStore.put("key1", "value1");
		nonemptyStore.put("key2", "value2");
		nonemptyStore.put("key3", "value3");
		nonemptyStore.restoreFromFile(XMLFilename);	// should work, store should be empty 
		try {
			getReturn = nonemptyStore.get("key1");
			System.out.println(getReturn);
			fail();
		} catch (KVException e) {
			// PASS (HOW DO YOU PASS?)
		}
		try {
			getReturn = nonemptyStore.get("key2");
			fail();
		} catch (KVException e) {
			// PASS (HOW DO YOU PASS?)
		}
		try {
			getReturn = nonemptyStore.get("key3");
			fail();
		} catch (KVException e) {
			// PASS (HOW DO YOU PASS?)
		}
		getReturn = nonemptyStore.get("key4");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value4"));
		getReturn = nonemptyStore.get("key5");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value5"));
		getReturn = nonemptyStore.get("key6");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value6"));
		getReturn = nonemptyStore.get("key7");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value7"));
		getReturn = nonemptyStore.get("key8");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value8"));
		getReturn = nonemptyStore.get("key9");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value9"));
		
		
		
		
		
		
		
		
		//Check store after call to restoreFromFile() with proper xml file with some key-value pairs with keys that DO match the ones in the store
		//store should just have whats inside xml file
		XMLFilename = "someKVPairFile.xml";
		writer = new BufferedWriter(new FileWriter(XMLFilename));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
				"<KVStore>" +
				"<KVPair><Key>key1</Key><Value>value1alt</Value></KVPair>" +
				"<KVPair><Key>key2</Key><Value>value2alt</Value></KVPair>" +
				"<KVPair><Key>key3</Key><Value>value3alt</Value></KVPair>" +
				"<KVPair><Key>key4</Key><Value>value4</Value></KVPair>" +
				"<KVPair><Key>key5</Key><Value>value5</Value></KVPair>" +
				"<KVPair><Key>key6</Key><Value>value6</Value></KVPair>" +
				"</KVStore>");		
		writer.close();
		
		nonemptyStore = new KVStore();
		nonemptyStore.put("key1", "value1");
		nonemptyStore.put("key2", "value2");
		nonemptyStore.put("key3", "value3");
		nonemptyStore.restoreFromFile(XMLFilename);	// should work, store should be empty 
	
		getReturn = nonemptyStore.get("key1");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value1alt"));
		getReturn = nonemptyStore.get("key2");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value2alt"));
		getReturn = nonemptyStore.get("key3");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value3alt"));
		getReturn = nonemptyStore.get("key4");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value4"));
		getReturn = nonemptyStore.get("key5");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value5"));
		getReturn = nonemptyStore.get("key6");
		assertTrue(getReturn != null);
		assertTrue(getReturn.equals("value6"));
		
		//Check store after call to restoreFromFile() with an invalid xml file
		//store should just be empty (?????) or exception thrown??
		
		



	}
	
	
}
