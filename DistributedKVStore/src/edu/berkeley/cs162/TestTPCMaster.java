package edu.berkeley.cs162;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import junit.framework.TestCase;

public class TestTPCMaster extends TestCase {
	//setting up a server, client, slaves...
	String server = "localhost";
	int port = 8080;			
	
	KVServer slave1 = new KVServer(10, 5);
	KVServer slave2 = new KVServer(10, 5);
	SocketServer socketserver1 = new SocketServer("localhost");
	SocketServer socketserver2 = new SocketServer("localhost");
	TPCMasterHandler handler1 = new TPCMasterHandler(slave1);
	TPCMasterHandler handler2 = new TPCMasterHandler(slave2);
	
	public class runServer implements Runnable {
		Thread t;
		runServer(){
			t = new Thread(this);
			t.start();
		}
		public void run() {
			startServer();
		}
		
	}
	
	public void startServer() {
		
		System.out.println("Binding Server:");
		TPCMaster master = new TPCMaster(2);
		master.run();
		System.out.println("hi");
		socketserver1.addHandler(handler1);
		socketserver2.addHandler(handler2);
		
		String logPath1 = 1 + "@" + socketserver1.getHostname();
		TPCLog tpcLog1 = new TPCLog(logPath1, slave1);
		String logPath2 = 2 + "@" + socketserver2.getHostname();
		TPCLog tpcLog2 = new TPCLog(logPath2, slave2);
		try {
			tpcLog1.rebuildKeyServer();
			handler1.setTPCLog(tpcLog1);
			socketserver1.connect();
			handler1.registerWithMaster(server, socketserver1);
			
			tpcLog2.rebuildKeyServer();
			handler2.setTPCLog(tpcLog2);
			socketserver2.connect();
			handler2.registerWithMaster(server, socketserver2);
		} catch (KVException e1) {
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
		
		try {
			System.out.println("Starting SlaveServer at " + socketserver1.getHostname() + ":" + socketserver1.getPort());
			Thread t1 = new Thread(this);
			socketserver1.run();

			System.out.println("Starting SlaveServer at " + socketserver2.getHostname() + ":" + socketserver2.getPort());
			socketserver2.run();
		}
		catch (IOException e) {
			System.out.println("Network Error");
		}
	}
	
	public void closeServer() {
		System.out.println("Closing server");
		socketserver1.stop();
		socketserver2.stop();
		try {
		Thread.sleep(10000);
		} catch (Exception e){
			
		}
	}
	
	@Test
	public void testHandleGet1() {
		new runServer();
		KVClient client = new KVClient(server, port);
		String val = null;
		System.out.println("handleget1");
		
		try {
			Thread.sleep(10000);
			client.put("fuzzy", "wuzzy");
			val = client.get("fuzzy");
			System.out.println("Value is: " + val);
			assertEquals(val, "wuzzy");
			client.del("fuzzy");		//need to clear server
			closeServer();
		}
		catch (Exception e){
			System.out.println("Value is: " + val);
			fail("Shouldn't have failed");
		}
	}
	
	@Test
	public void testHandleGet2() {
		new runServer();
		KVClient client = new KVClient(server, port);	
		System.out.println("handleget2s");

		try {
			//test get on key that doesn't exist
			client.get("supah");
			closeServer();
		}
		catch (KVException e){
			String message = e.getMsg().getMessage();
			assertTrue(message.equals("Does not exist"));
		}
	}
	
	
	@Test
	public void testIsParseable1() {
		TPCMaster master = new TPCMaster(2);
		KVMessage regMsg = null;
		try {
			regMsg = new KVMessage("register");
		} catch (KVException e) {
			fail("Shouldn't have failed");
		}
		regMsg.setMessage("1@localhost:1010");
		try {
			master.isParseable(regMsg);
		} catch (KVException e) {
			fail("Shouldn't have failed");
		}
	}
	
	@Test
	public void testIsParseable2() {
		TPCMaster master = new TPCMaster(2);
		KVMessage regMsg = null;
		try {
			regMsg = new KVMessage("register");
		} catch (KVException e) {
			fail("Shouldn't have failed");
		}
		//testing bad input for slave ID
		regMsg.setMessage("SlaveServerID@localhost:1010");
		try {
			master.isParseable(regMsg);
		} catch (KVException e) {
			String message = e.getMsg().getMessage();
			assertTrue(message.equals("Registration Error: Received unparseable slave information"));
		}
	}

	@Test
	public void testIsParseable3() {
		TPCMaster master = new TPCMaster(2);
		KVMessage regMsg = null;
		try {
			regMsg = new KVMessage("register");
		} catch (KVException e) {
			fail("Shouldn't have failed");
		}
		//testing null input for slave ID and port
		regMsg.setMessage("@local3host:");
		try {
			master.isParseable(regMsg);
		} catch (KVException e) {
			String message = e.getMsg().getMessage();
			assertTrue(message.equals("Registration Error: Received unparseable slave information"));
		}
	}


}
