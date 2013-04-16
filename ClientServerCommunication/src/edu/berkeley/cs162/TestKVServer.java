package edu.berkeley.cs162;

import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;

import junit.framework.TestCase;

public class TestKVServer extends TestCase {
	
	KVServer server = new KVServer(10, 10);
	
	//
	//checkKey() tests
	//
	public void testNullKey() {
		try {
			server.checkKey(null);
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Key", e.getMsg().getMessage());
		}
	}
	
	public void testOversizedKey() throws KVException {
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
		try {
			server.checkValue(null);
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Value", e.getMsg().getMessage());
		}
	}
	
	public void testOversizedValue() throws KVException {
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
		try {
			server.checkValue("");
		} catch (KVException e) {
			assertEquals("Unknown Error: Zero Size Value", e.getMsg().getMessage());
		}
	}
	
	//
	//put() tests
	//
	@Test
	public void testPut1() {
		String a = "";
		try {
			System.out.println("HI");
			server.put("1", "hello");
			System.out.println("BYE");
			a = server.get("1");
			assertEquals("hello", a);
		} catch (KVException e) {
		}
	}
	
	@Test
	public void putTest2() throws KVException {
		server.put("1", "hello");
		server.put("1", "world");
		assertEquals("world", server.get("1"));
		server.del("1");
	}
	
	@Test (expected = KVException.class)
	public void badPutTest1() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Null Key"));
		server.put(null, "hello");
	}

	@Test (expected = KVException.class)
	public void badPutTest2() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Null Value"));
		server.put("1", null);
	}
	
	//
	//get() tests
	//
	@Test
	public void getTest1() throws KVException {
		assertEquals(null, server.get("10"));
	}
	
	@Test (expected = KVException.class)
	public void badGetTest1() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Zero Size Key"));
		server.get("");
	}

	@Test (expected = KVException.class)
	public void badGetTest2() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Null Key"));
		server.get(null);
	}

	//
	//del() tests
	//
	@Test
	public void delTest1() throws KVException {
		server.put("1", "hello");
		server.del("1");
		assertEquals(null, server.get("1"));
	}
	
	@Test (expected = KVException.class)
	public void badDelTest1() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Null Key"));
		server.del(null);
	}

	@Test (expected = KVException.class)
	public void badDelTest2() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Zero Size Key"));
		server.del("");
	}

}
