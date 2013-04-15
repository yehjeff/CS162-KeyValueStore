package edu.berkeley.cs162;

import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;

import junit.framework.TestCase;

public class TestKVServer extends TestCase {
	
	KVServer server = new KVServer(10, 10);

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	//
	//checkKey() tests
	//
	@Test (expected = KVException.class)
	public void nullKeyTest() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Null Key"));
		server.checkKey(null);
	}
	
	@Test (expected = KVException.class)
	public void oversizedKeyTest() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Oversized key"));
		String overSize = null;
		for (int i = 0; i < 256; i++) {
			overSize += "k";
		}
		server.checkKey(overSize);
	}

	@Test (expected = KVException.class)
	public void zeroKeyTest() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Zero Size Key"));
		server.checkKey("");
	}

	//
	//checkValue() tests
	//
	@Test (expected = KVException.class)
	public void nullValueTest() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Null Value"));
		server.checkValue(null);
	}
	
	@Test (expected = KVException.class)
	public void oversizedValueTest() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Oversized value"));
		String overSize = null;
		for (int i = 0; i < 256 * 1024; i++) {
			overSize += "k";
		}
		server.checkValue(overSize);
	}

	@Test (expected = KVException.class)
	public void zeroValueTest() throws KVException {
		thrown.expectMessage(JUnitMatchers.containsString("Unknown Error: Zero Size Value"));
		server.checkValue("");
	}
	
	//
	//put() tests
	//
	@Test
	public void putTest1() throws KVException {
		server.put("1", "hello");
		assertEquals("hello", server.get("1"));
		server.del("1");
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
