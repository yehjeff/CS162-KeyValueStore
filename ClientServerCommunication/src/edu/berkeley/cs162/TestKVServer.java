package edu.berkeley.cs162;

import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;

import junit.framework.TestCase;

public class TestKVServer extends TestCase {
	
	//
	//checkKey() tests
	//
	public void testNullKey() {
		KVServer server = new KVServer(10, 10);
		try {
			server.checkKey(null);
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Key", e.getMsg().getMessage());
		}
	}
	
	public void testOversizedKey() throws KVException {
		KVServer server = new KVServer(10, 10);
		String overSize = null;
		for (int i = 0; i < 256; i++) {
			overSize += "k";
		}
		try {
			server.checkKey(overSize);
		} catch (KVException e) {
			assertEquals("Oversized key", e.getMsg().getMessage());
		}
	}

	public void testZeroKey() throws KVException {
		KVServer server = new KVServer(10, 10);
		try {
			server.checkKey("");
		} catch (KVException e) {
			assertEquals("Unknown Error: Zero Size Key", e.getMsg().getMessage());
		}
	}

	//
	//checkValue() tests
	//
	public void testNullValue() {
		KVServer server = new KVServer(10, 10);
		try {
			server.checkValue(null);
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Value", e.getMsg().getMessage());
		}
	}
	
	public void testOversizedValue() throws KVException {
		KVServer server = new KVServer(10, 10);
		String overSize = null;
		for (int i = 0; i < 1024; i++) {
			overSize += "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk";
		}
		try {
			server.checkValue(overSize);
		} catch (KVException e) {
			assertEquals("Oversized value", e.getMsg().getMessage());
		}
	}

	public void testZeroValue() throws KVException {
		KVServer server = new KVServer(10, 10);
		try {
			server.checkValue("");
		} catch (KVException e) {
			assertEquals("Unknown Error: Zero Size Value", e.getMsg().getMessage());
		}
	}
	
	//
	//put() tests
	//
	public void testPut1() {
		KVServer server = new KVServer(10, 10);
		try {
			server.put("1", "hello");
			assertEquals("hello", server.get("1"));
		} catch (KVException e) {
		}
	}
	
	public void testPut2() throws KVException {
		KVServer server = new KVServer(10, 10);
		try {
			server.put("1", "hello");
			server.put("1", "world");
			assertEquals("world", server.get("1"));
		} catch (KVException e) {
		}
	}
	
	public void testBadPut1() {
		KVServer server = new KVServer(10, 10);
		try {
			server.put(null, "hello");
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Key", e.getMsg().getMessage());
		}
	}

	public void testBadPut2() throws KVException {
		KVServer server = new KVServer(10, 10);
		try {
			server.put("1", null);
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Value", e.getMsg().getMessage());
		}
	}
	
	//
	//get() tests
	//

	public void testGet() {
		KVServer server = new KVServer(10, 10);
		try {
			server.put("2", "hi");
			assertEquals("hi", server.get("2"));
		} catch (KVException e) {
		}
	}
	
	public void testBadGet1() {
		KVServer server = new KVServer(10, 10);
		try {
			server.get("10");
			fail();
		} catch (KVException e) {
			assertEquals("Does not exist", e.getMsg().getMessage());
		}
	}
	
	public void testBadGet2() {
		KVServer server = new KVServer(10, 10);
		try {
			server.get("");
		} catch (KVException e) {
			assertEquals("Unknown Error: Zero Size Key", e.getMsg().getMessage());
		}
	}

	public void testBadGet3() {
		KVServer server = new KVServer(10, 10);
		try {
			server.get(null);
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Key", e.getMsg().getMessage());
		}
	}

	//
	//del() tests
	//
	public void testDel() {
		KVServer server = new KVServer(10, 10);
		try {
			server.put("1", "hello");
			server.del("1");
			assertEquals(null, server.get("1"));
		} catch (KVException e) {
		}
	}

	public void testBadDel1() {
		KVServer server = new KVServer(10, 10);
		try {
			server.del("10");
		} catch (KVException e) {
			assertEquals("Does not exist", e.getMsg().getMessage());
		}
	}
	
	public void testBadDel2() {
		KVServer server = new KVServer(10, 10);
		try {
			server.del(null);
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Key", e.getMsg().getMessage());
		}
	}

	public void testBadDel3() {
		KVServer server = new KVServer(10, 10);
		try {
			server.del("");
		} catch (KVException e) {
			assertEquals("Unknown Error: Zero Size Key", e.getMsg().getMessage());
		}
	}

}
