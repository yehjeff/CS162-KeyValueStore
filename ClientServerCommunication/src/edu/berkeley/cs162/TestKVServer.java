package edu.berkeley.cs162;

import junit.framework.TestCase;

public class TestKVServer extends TestCase {
	
	//
	//checkKey() tests
	//
	public void testNullKey() {
		KVServer server = new KVServer(10, 10);
		try {
			server.checkKey(null);
			fail();		//never gets here
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
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Oversized key", e.getMsg().getMessage());
		}
	}

	public void testZeroKey() throws KVException {
		KVServer server = new KVServer(10, 10);
		try {
			server.checkKey("");
			fail();		//never gets here
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
			fail();		//never gets here
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
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Oversized value", e.getMsg().getMessage());
		}
	}

	public void testZeroValue() throws KVException {
		KVServer server = new KVServer(10, 10);
		try {
			server.checkValue("");
			fail();		//never gets here
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
			fail();		//never gets here
		}
	}
	
	public void testPut2() throws KVException {
		KVServer server = new KVServer(10, 10);
		try {
			server.put("1", "hello");
			server.put("1", "world");
			assertEquals("world", server.get("1"));
		} catch (KVException e) {
			fail();		//never gets here
		}
	}
	
	public void testBadPut1() {
		KVServer server = new KVServer(10, 10);
		try {
			server.put(null, "hello");
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Key", e.getMsg().getMessage());
		}
	}

	public void testBadPut2() throws KVException {
		KVServer server = new KVServer(10, 10);
		try {
			server.put("1", null);
			fail();		//never gets here
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
			fail();		//never gets here
		}
	}
	
	public void testBadGet1() {
		KVServer server = new KVServer(10, 10);
		try {
			server.get("10");
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Does not exist", e.getMsg().getMessage());
		}
	}
	
	public void testBadGet2() {
		KVServer server = new KVServer(10, 10);
		try {
			server.get("");
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Unknown Error: Zero Size Key", e.getMsg().getMessage());
		}
	}

	public void testBadGet3() {
		KVServer server = new KVServer(10, 10);
		try {
			server.get(null);
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Key", e.getMsg().getMessage());
		}
	}

	//
	//del() tests
	//
	public void testDel() {
		KVServer server = new KVServer(10, 10);
		String a = "10";
		try {
			server.put("1", "hello");
			server.del("1");
			a = server.get("1");
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Does not exist", e.getMsg().getMessage());
		}
	}

	public void testBadDel1() {
		KVServer server = new KVServer(10, 10);
		try {
			server.del("10");
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Does not exist", e.getMsg().getMessage());
		}
	}
	
	public void testBadDel2() {
		KVServer server = new KVServer(10, 10);
		try {
			server.del(null);
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Unknown Error: Null Key", e.getMsg().getMessage());
		}
	}

	public void testBadDel3() {
		KVServer server = new KVServer(10, 10);
		try {
			server.del("");
			fail();		//never gets here
		} catch (KVException e) {
			assertEquals("Unknown Error: Zero Size Key", e.getMsg().getMessage());
		}
	}

}
