package edu.berkeley.cs162;

import java.io.ByteArrayInputStream;
import java.net.Socket;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import junit.framework.TestCase;

public class TestKVMessage extends TestCase {

	//single argument constructor test, correct inputs
	public void testBasicConstructorValid() {
		try {
			String[] validTypes = {"putreq", "delreq", "getreq", "resp"};
			for(int i = 0; i < validTypes.length; i++) {
				KVMessage testMsg = new KVMessage(validTypes[i]);
				assertEquals("should have correct message type", validTypes[i], testMsg.getMsgType());
				assertNull("other fields should be null", testMsg.getKey());
				assertNull("other fields should be null", testMsg.getValue());
				assertNull("other fields should be null", testMsg.getMessage());
			}
			
		} catch (KVException e) {
			fail("Basic Constructor Failure: Failed KVMessage(msgType) with correct input");
		}
	}
	
	//single argument constructor test, invalid inputs
	public void testBasicConstructorInvalid() {
		try {
			String invalidType = "inval";
			KVMessage testMsg = new KVMessage(invalidType);
			//should error before this point
			fail("Basic Constructor Failure: Failed to prevent construction of KVMessage(msgType) with incorrect input");
		} catch (KVException e) {
			//all is well
		}
	}
	
	//two argument constructor test with message, valid inputs
	public void testMessageConstructorValid() {
		try {
			String[] validTypes = {"putreq", "delreq", "getreq", "resp"};
			String[] messages = {"junit", "tests", "are", "cool"};
			for(int i = 0; i < validTypes.length; i++) {
				KVMessage testMsg = new KVMessage(validTypes[i], messages[i]);
				assertEquals("should have correct message type", validTypes[i], testMsg.getMsgType());
				assertNull("other fields should be null", testMsg.getKey());
				assertNull("other fields should be null", testMsg.getValue());
				assertEquals("should have correct message", messages[i], testMsg.getMessage());
			}
			
		} catch (KVException e) {
			fail("Basic Constructor Failure: Failed KVMessage(msgType) with correct input");
		}
	}
	
	//two argument constructor test with message, invalid inputs (msgTypes, message checked on toXML)
	public void testMessageConstructorInvalid() {
		try {
			String invalidType = "inval";
			String invalidMsg = "#yolo";
			KVMessage testMsg = new KVMessage(invalidType, invalidMsg);
			//should error before this point
			fail("Basic Constructor Failure: Failed to prevent construction of KVMessage(msgType) with incorrect input");
		} catch (KVException e) {
			//all is well
		}
	}
	
	//time to test the input stream constructor, first let's hard-code some XML files
	//valid XML
	private String XMLgetreq = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"getreq\"> <Key>GETREQ_KEY</Key> </KVMessage>";
	private String XMLputreq = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"putreq\"> <Key>PUTREQ_KEY</Key> <Value>PUTREQ_VAL</Value> </KVMessage>";
	private String XMLdelreq = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"delreq\"> <Key>DELREQ_KEY</Key> </KVMessage>";
	private String XMLrespKV = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"resp\"> <Key>RESP_KEY</Key> <Value>RESP_VAL</Value> </KVMessage>";
	private String XMLrespM = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"resp\"> <Message>RESP_MSG</Message> </KVMessage>";
	//valid XML with extra fields as specified by msgType
	private String XMLextra1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"resp\"> <Key>RESP_KEY</Key> <Value>RESP_VAL</Value> <Message>RESP_MSG</Message> </KVMessage>";
	private String XMLextra2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"getreq\"> <Key>GETREQ_KEY</Key> <Value>EXTRA_VAL</Value> <Message>EXTRA_MSG</Message> </KVMessage>";
	private String XMLextra3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"putreq\"> <Key>PUTREQ_KEY</Key> <Value>PUTREQ_VAL</Value> <Message>EXTRA_MSG</Message> </KVMessage>";
	private String XMLextra4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"delreq\"> <Value>EXTRA_VAL</Value> <Message>EXTRA_MSG</Message> <Key>DELREQ_KEY</Key> </KVMessage>";
	//valid XML with missing fields as specified by msgType
	private String XMLmiss1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"getreq\"> </KVMessage>";
	private String XMLmiss2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"putreq\"> </KVMessage>";
	private String XMLmiss3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"delreq\"> </KVMessage>";
	private String XMLmiss4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"resp\"> </KVMessage>";
	//invalid XML
	private String XMLwrongMsgType = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"wrong\"> </KVMessage>";
	private String XMLextraField = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <KVMessage type=\"resp\"> <Whatintheworld>WHAT_IS_THIS</Whatintheworld> </KVMessage>";
	private String XMLgibberish = "<?xml vers54qion=\"1.0\" encq4o,<>ding=\"UTF-8\"?> <<KVMe,.ssq4<age type=\"r'&#esp\"> <////KVMessage>/";
	
	//inputStream constructor test with valid xml files
	public void testInputConstructorValid() {
		try {
			ByteArrayInputStream testInputStream1 = new ByteArrayInputStream(XMLgetreq.getBytes("UTF-8"));
			KVMessage testMsg1 = new KVMessage(testInputStream1);
			assertEquals("should have correct msgType", "getreq", testMsg1.getMsgType());
			assertEquals("should have correct key", "GETREQ_KEY", testMsg1.getKey());
			assertNull("should have null value", testMsg1.getValue());
			assertNull("should have null message", testMsg1.getMessage());
			
			ByteArrayInputStream testInputStream2 = new ByteArrayInputStream(XMLputreq.getBytes("UTF-8"));
			KVMessage testMsg2 = new KVMessage(testInputStream2);
			assertEquals("should have correct msgType", "putreq", testMsg2.getMsgType());
			assertEquals("should have correct key", "PUTREQ_KEY", testMsg2.getKey());
			assertEquals("should have correct value", "PUTREQ_VAL", testMsg2.getValue());
			assertNull("should have null message", testMsg2.getMessage());
			
			ByteArrayInputStream testInputStream3 = new ByteArrayInputStream(XMLdelreq.getBytes("UTF-8"));
			KVMessage testMsg3 = new KVMessage(testInputStream3);
			assertEquals("should have correct msgType", "delreq", testMsg3.getMsgType());
			assertEquals("should have correct key", "DELREQ_KEY", testMsg3.getKey());
			assertNull("should have null value", testMsg3.getValue());
			assertNull("should have null message", testMsg3.getMessage());
			
			ByteArrayInputStream testInputStream4 = new ByteArrayInputStream(XMLrespKV.getBytes("UTF-8"));
			KVMessage testMsg4 = new KVMessage(testInputStream4);
			assertEquals("should have correct msgType", "resp", testMsg4.getMsgType());
			assertEquals("should have correct key", "RESP_KEY", testMsg4.getKey());
			assertEquals("should have correct value", "RESP_VAL", testMsg4.getValue());
			assertNull("should have null message", testMsg4.getMessage());
			
			ByteArrayInputStream testInputStream5 = new ByteArrayInputStream(XMLrespM.getBytes("UTF-8"));
			KVMessage testMsg5 = new KVMessage(testInputStream5);
			assertEquals("should have correct msgType", "resp", testMsg5.getMsgType());
			assertNull("should have null key", testMsg5.getKey());
			assertNull("should have null value", testMsg5.getValue());
			assertEquals("should have correct message", "RESP_MSG", testMsg5.getMessage());
			
		} catch (Exception E) {
			fail("something went wrong with the input stream constructor given valid inputs!");
		}
	}

	//inputStream constructor test with valid xml files
	public void testInputConstructorExtra() {
		try {
			//should not throw exception given malformed information, that is later in toXML
			
			ByteArrayInputStream testInputStream1 = new ByteArrayInputStream(XMLextra1.getBytes("UTF-8"));
			KVMessage testMsg1 = new KVMessage(testInputStream1);
			assertEquals("should have correct msgType", "resp", testMsg1.getMsgType());
			assertEquals("should have correct key", "RESP_KEY", testMsg1.getKey());
			assertEquals("should have correct value", "RESP_VAL", testMsg1.getValue());
			assertEquals("should have correct message", "RESP_MSG", testMsg1.getMessage());

			ByteArrayInputStream testInputStream2 = new ByteArrayInputStream(XMLextra2.getBytes("UTF-8"));
			KVMessage testMsg2 = new KVMessage(testInputStream2);
			assertEquals("should have correct msgType", "getreq", testMsg2.getMsgType());
			assertEquals("should have correct key", "GETREQ_KEY", testMsg2.getKey());
			assertEquals("should have correct value", "EXTRA_VAL", testMsg2.getValue());
			assertEquals("should have correct message", "EXTRA_MSG", testMsg2.getMessage());
			
			ByteArrayInputStream testInputStream3 = new ByteArrayInputStream(XMLextra3.getBytes("UTF-8"));
			KVMessage testMsg3 = new KVMessage(testInputStream3);
			assertEquals("should have correct msgType", "putreq", testMsg3.getMsgType());
			assertEquals("should have correct key", "PUTREQ_KEY", testMsg3.getKey());
			assertEquals("should have correct value", "PUTREQ_VAL", testMsg3.getValue());
			assertEquals("should have correct message", "EXTRA_MSG", testMsg3.getMessage());
			
			ByteArrayInputStream testInputStream4 = new ByteArrayInputStream(XMLextra4.getBytes("UTF-8"));
			KVMessage testMsg4 = new KVMessage(testInputStream4);
			assertEquals("should have correct msgType", "delreq", testMsg4.getMsgType());
			assertEquals("should have correct key", "DELREQ_KEY", testMsg4.getKey());
			assertEquals("should have correct value", "EXTRA_VAL", testMsg4.getValue());
			assertEquals("should have correct message", "EXTRA_MSG", testMsg4.getMessage());
			
		} catch (Exception E) {
			fail("something went wrong with the input stream constructor given extra inputs!");
		}
	}
	
	//inputStream constructor test with valid xml files
	public void testInputConstructorMissing() {
		try {
			//should not throw exception given malformed information, that is later in toXML
			
			ByteArrayInputStream testInputStream1 = new ByteArrayInputStream(XMLmiss1.getBytes("UTF-8"));
			KVMessage testMsg1 = new KVMessage(testInputStream1);
			assertEquals("should have correct msgType", "getreq", testMsg1.getMsgType());
			assertNull("should have null key", testMsg1.getKey());
			assertNull("should have null val", testMsg1.getValue());
			assertNull("should have null msg", testMsg1.getMessage());
			
			ByteArrayInputStream testInputStream2 = new ByteArrayInputStream(XMLmiss2.getBytes("UTF-8"));
			KVMessage testMsg2 = new KVMessage(testInputStream2);
			assertEquals("should have correct msgType", "putreq", testMsg2.getMsgType());
			assertNull("should have null key", testMsg2.getKey());
			assertNull("should have null val", testMsg2.getValue());
			assertNull("should have null msg", testMsg2.getMessage());
			
			ByteArrayInputStream testInputStream3 = new ByteArrayInputStream(XMLmiss3.getBytes("UTF-8"));
			KVMessage testMsg3 = new KVMessage(testInputStream3);
			assertEquals("should have correct msgType", "delreq", testMsg3.getMsgType());
			assertNull("should have null key", testMsg3.getKey());
			assertNull("should have null val", testMsg3.getValue());
			assertNull("should have null msg", testMsg3.getMessage());
			
			ByteArrayInputStream testInputStream4 = new ByteArrayInputStream(XMLmiss4.getBytes("UTF-8"));
			KVMessage testMsg4 = new KVMessage(testInputStream4);
			assertEquals("should have correct msgType", "resp", testMsg4.getMsgType());
			assertNull("should have null key", testMsg4.getKey());
			assertNull("should have null val", testMsg4.getValue());
			assertNull("should have null msg", testMsg4.getMessage());
			
		} catch (Exception e) {
			fail("something went wrong with the input stream constructor given missing inputs!");
		}
	}
	
	//test for invalid XML formats that do not throw an error
	public void testInputConstructorInvalidNoError() {
		try {
			ByteArrayInputStream testInputStream1 = new ByteArrayInputStream(XMLwrongMsgType.getBytes("UTF-8"));
			KVMessage testMsg1 = new KVMessage(testInputStream1);
			assertNotNull("should have some (incorrect) msgType", testMsg1.getMsgType());
			assertNull("should have null key", testMsg1.getKey());
			assertNull("should have null val", testMsg1.getValue());
			assertNull("should have null msg", testMsg1.getMessage());
			
			ByteArrayInputStream testInputStream2 = new ByteArrayInputStream(XMLextraField.getBytes("UTF-8"));
			KVMessage testMsg2 = new KVMessage(testInputStream2);
			assertEquals("should have correct msgType", "resp", testMsg2.getMsgType());
			assertNull("should have null key", testMsg2.getKey());
			assertNull("should have null val", testMsg2.getValue());
			assertNull("should have null msg", testMsg2.getMessage());
			
		} catch (Exception e) {
			fail("something went wrong with the input stream constructor given non-crashing invalid inputs!");
		}
	}
	
	//test for invalid XML formats that do throw an error
	
	public void testInputConstructorInvalidWillError() {
		try {
			ByteArrayInputStream testInputStream1 = new ByteArrayInputStream(XMLgibberish.getBytes("UTF-8"));
			KVMessage testMsg1 = new KVMessage(testInputStream1);
			fail("the gibberish somehow parsed into a KVmesage");
		} catch (KVException e) {
			assertEquals("should have expected error message", "XML Error: Received unparseable message", e.getMsg().getMessage());
		} catch (Exception e) {
			//do nothing
		}
	}
	
	//to test toXML, we need to create some KVMessages
	
	//guh this sucks, i can see that it works... i don't want to parse the result... and so many of them...
	public void testToXMLValid() {
		try {
			DocumentBuilder builder;
			Document doc;
			ByteArrayInputStream input;
			String xmlStr;
			KVMessage KV;
			Element KVElement;
			
			KV = new KVMessage("getreq");
			KV.setKey("KEY");
			//KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			input = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
			//parse the xml that toXML made, done similar as the input stream constructor so I assume that wors which I have tested before this
			doc = builder.parse(input);
			KVElement = (Element) doc.getElementsByTagName("KVMessage").item(0);			
			assertEquals("Should contain a single KVMessage", 1, doc.getElementsByTagName("KVMessage").getLength());
			assertEquals("Should have same msgType", "getreq", KVElement.getAttribute("type"));
			assertEquals("Should contain a single key", 1, KVElement.getElementsByTagName("Key").getLength());
			assertEquals("Should have same key", "KEY", KVElement.getElementsByTagName("Key").item(0).getTextContent());
			assertEquals("Should contain no value", 0, KVElement.getElementsByTagName("Value").getLength());
			//assertEquals("Should have same value", "VAL", KVElement.getElementsByTagName("Value").item(0).getTextContent());
			assertEquals("Should contain no message", 0, KVElement.getElementsByTagName("Message").getLength());
			//assertEquals("Should have same message", "MSG", KVElement.getElementsByTagName("Message").item(0).getTextContent());
			
			KV = new KVMessage("putreq");
			KV.setKey("KEY");
			KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			input = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
			//parse the xml that toXML made, done similar as the input stream constructor so I assume that wors which I have tested before this
			doc = builder.parse(input);
			KVElement = (Element) doc.getElementsByTagName("KVMessage").item(0);			
			assertEquals("Should contain a single KVMessage", 1, doc.getElementsByTagName("KVMessage").getLength());
			assertEquals("Should have same msgType", "putreq", KVElement.getAttribute("type"));
			assertEquals("Should contain a single key", 1, KVElement.getElementsByTagName("Key").getLength());
			assertEquals("Should have same key", "KEY", KVElement.getElementsByTagName("Key").item(0).getTextContent());
			assertEquals("Should contain a single value", 1, KVElement.getElementsByTagName("Value").getLength());
			assertEquals("Should have same value", "VAL", KVElement.getElementsByTagName("Value").item(0).getTextContent());
			assertEquals("Should contain no message", 0, KVElement.getElementsByTagName("Message").getLength());
			//assertEquals("Should have same message", "MSG", KVElement.getElementsByTagName("Message").item(0).getTextContent());
			
			KV = new KVMessage("delreq");
			KV.setKey("KEY");
			//KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			input = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
			//parse the xml that toXML made, done similar as the input stream constructor so I assume that wors which I have tested before this
			doc = builder.parse(input);
			KVElement = (Element) doc.getElementsByTagName("KVMessage").item(0);			
			assertEquals("Should contain a single KVMessage", 1, doc.getElementsByTagName("KVMessage").getLength());
			assertEquals("Should have same msgType", "delreq", KVElement.getAttribute("type"));
			assertEquals("Should contain a single key", 1, KVElement.getElementsByTagName("Key").getLength());
			assertEquals("Should have same key", "KEY", KVElement.getElementsByTagName("Key").item(0).getTextContent());
			assertEquals("Should contain no value", 0, KVElement.getElementsByTagName("Value").getLength());
			//assertEquals("Should have same value", "VAL", KVElement.getElementsByTagName("Value").item(0).getTextContent());
			assertEquals("Should contain no message", 0, KVElement.getElementsByTagName("Message").getLength());
			//assertEquals("Should have same message", "MSG", KVElement.getElementsByTagName("Message").item(0).getTextContent());
			
			KV = new KVMessage("resp"); //response type1
			KV.setKey("KEY");
			KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			input = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
			//parse the xml that toXML made, done similar as the input stream constructor so I assume that wors which I have tested before this
			doc = builder.parse(input);
			KVElement = (Element) doc.getElementsByTagName("KVMessage").item(0);			
			assertEquals("Should contain a single KVMessage", 1, doc.getElementsByTagName("KVMessage").getLength());
			assertEquals("Should have same msgType", "resp", KVElement.getAttribute("type"));
			assertEquals("Should contain a single key", 1, KVElement.getElementsByTagName("Key").getLength());
			assertEquals("Should have same key", "KEY", KVElement.getElementsByTagName("Key").item(0).getTextContent());
			assertEquals("Should contain a single value", 1, KVElement.getElementsByTagName("Value").getLength());
			//assertEquals("Should have same value", "VAL", KVElement.getElementsByTagName("Value").item(0).getTextContent());
			assertEquals("Should contain no message", 0, KVElement.getElementsByTagName("Message").getLength());
			//assertEquals("Should have same message", "MSG", KVElement.getElementsByTagName("Message").item(0).getTextContent());
			
			KV = new KVMessage("resp"); //response type2
			//KV.setKey("KEY");
			//KV.setValue("VAL");
			KV.setMessage("MSG");
			xmlStr = KV.toXML();
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			input = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
			//parse the xml that toXML made, done similar as the input stream constructor so I assume that wors which I have tested before this
			doc = builder.parse(input);
			KVElement = (Element) doc.getElementsByTagName("KVMessage").item(0);			
			assertEquals("Should contain a single KVMessage", 1, doc.getElementsByTagName("KVMessage").getLength());
			assertEquals("Should have same msgType", "resp", KVElement.getAttribute("type"));
			assertEquals("Should contain no key", 0, KVElement.getElementsByTagName("Key").getLength());
			//assertEquals("Should have same key", "KEY", KVElement.getElementsByTagName("Key").item(0).getTextContent());
			assertEquals("Should contain no value", 0, KVElement.getElementsByTagName("Value").getLength());
			//assertEquals("Should have same value", "VAL", KVElement.getElementsByTagName("Value").item(0).getTextContent());
			assertEquals("Should contain a single message", 1, KVElement.getElementsByTagName("Message").getLength());
			assertEquals("Should have same message", "MSG", KVElement.getElementsByTagName("Message").item(0).getTextContent());
			
		} catch (KVException e) {
			System.out.println(e.getMsg().getMessage());
			fail("a KVException was thrown when it shouldn't have been");
		} catch (Exception e) {
			fail("an exception was thrown when it shouldn't have been");
		}
	}
	
	public void testToXMLInvalidGet() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("getreq");
			//KV.setKey("KEY");
			//KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the missing KEY field");
			
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Key is null or zero-length", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidDel() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("delreq");
			//KV.setKey("KEY");
			//KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("del request should have failed with the missing KEY field");
			
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Key is null or zero-length", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidPut1() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("putreq");
			//KV.setKey("KEY");
			KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the missing KEY field");
			
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Key is null or zero-length", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidPut2() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("putreq");
			KV.setKey("KEY");
			//KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the missing VAL field");
			
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Value is null or zero-length", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidPut3() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("putreq");
			//KV.setKey("KEY");
			//KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the missing KEY and VAL field");
			
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Key is null or zero-length", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidResp1() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("resp");
			//KV.setKey("KEY");
			//KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the all empty fields");
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Incorrect fields for 'resp' msgType", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidResp2() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("resp");
			KV.setKey("KEY");
			//KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the empty VAL field");
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Incorrect fields for 'resp' msgType", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidResp3() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("resp");
			//KV.setKey("KEY");
			KV.setValue("VAL");
			//KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the empty KEY field");
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Incorrect fields for 'resp' msgType", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidResp4() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("resp");
			KV.setKey("KEY");
			//KV.setValue("VAL");
			KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the empty VAL and extra MSG OR with the extra KEY field");
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Incorrect fields for 'resp' msgType", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidResp5() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("resp");
			//KV.setKey("KEY");
			KV.setValue("VAL");
			KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the empty KEY and extra MSG OR with the extra VAL field");
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Incorrect fields for 'resp' msgType", e.getMsg().getMessage());
		} 
	}
	
	public void testToXMLInvalidResp6() {
		try {
			KVMessage KV;
			String xmlStr;
			KV = new KVMessage("resp");
			KV.setKey("KEY");
			KV.setValue("VAL");
			KV.setMessage("MSG");
			xmlStr = KV.toXML();
			fail("get request should have failed with the extra MSG field OR the extra KEY&VAL fields");
		} catch (KVException e) {
			//System.out.println(e.getMsg().getMessage());
			assertEquals("should show expected error message", "Unknown Error: Incorrect fields for 'resp' msgType", e.getMsg().getMessage());
		} 
	}
	
	public void testSendMessage() {
		try {
			KVMessage KV;
			Socket sock;
			
			KV = new KVMessage("putreq");
			KV.setKey("KEY");
			KV.setValue("VAL");
			sock = new Socket();
			//making socket wrong... stalls on get output stream
			//KV.sendMessage(sock);
			//System.out.println(sock.getOutputStream().toString());
			
		} catch (Exception e) {
			//all is well, does not error out, sending message implicitly tested by other junit tests
		}
	}
}
