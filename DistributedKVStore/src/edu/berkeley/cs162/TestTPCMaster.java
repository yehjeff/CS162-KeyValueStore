package edu.berkeley.cs162;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import junit.framework.TestCase;

public class TestTPCMaster extends TestCase {
	//setting up a server, client, slaves...
	String server;
	int port;			

	TPCMaster master;
	KVServer slave1;
	KVServer slave2;
	SocketServer masterserver;
	SocketServer slaveserver1;
	SocketServer slaveserver2;
	NetworkHandler handler;
	TPCMasterHandler handler1;
	TPCMasterHandler handler2;
	
	public class runMaster implements Runnable {
		
		public void run() {
			master.run();
			System.out.println("Binding Master:");
			masterserver.addHandler(handler);
			try {
				masterserver.connect();
				System.out.println("Starting Master");
				masterserver.run();		
			} catch (IOException e) {
			}
		}
	}
	public void closeMaster() {
		System.out.println("Closing master");
		masterserver.stop();
		master.stop();
		try {
		Thread.sleep(10000);
		} catch (Exception e){
		}
	}

	
	public class runSlaveServer implements Runnable {
	
		int _id;
		runSlaveServer(int id){
			_id = id;
		
		}
		public void run() {
			try {
				switch(_id) {
				case 1:
					slaveserver1.run();
					break;
				case 2:
					slaveserver2.run();
					break;
				} 
			} catch (IOException e) {
			}
		}
	}
	public class runServers implements Runnable {
	
		public void run() {
			startServers();
		}
		
	}
	public void startServers() {
		
		slaveserver1.addHandler(handler1);
		slaveserver2.addHandler(handler2);
		
		String logPath1 = 1 + "@" + slaveserver1.getHostname();
		TPCLog tpcLog1 = new TPCLog(logPath1, slave1);
		String logPath2 = 2 + "@" + slaveserver2.getHostname();
		TPCLog tpcLog2 = new TPCLog(logPath2, slave2);
		try {
//			tpcLog1.rebuildKeyServer();
			handler1.setTPCLog(tpcLog1);
			slaveserver1.connect();
			handler1.registerWithMaster(server, slaveserver1);

//			tpcLog2.rebuildKeyServer();
			handler2.setTPCLog(tpcLog2);
			slaveserver2.connect();
			handler2.registerWithMaster(server, slaveserver2);
		} catch (KVException e1) {
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
		
		System.out.println("Starting SlaveServer at " + slaveserver1.getHostname() + ":" + slaveserver1.getPort());
		new Thread(new runSlaveServer(1)).start();
		

		System.out.println("Starting SlaveServer at " + slaveserver2.getHostname() + ":" + slaveserver2.getPort());
		new Thread(new runSlaveServer(2)).start();
		
	}
	public void closeServers() {
		System.out.println("Closing servers");
		slaveserver1.stop();
		handler1.stop();
		
		slaveserver2.stop();
		handler2.stop();
		try {
		//Thread.sleep(10000);
		} catch (Exception e){
		}
	}
	

	@Test
	public void testHandleGet1() {
		server = "localhost";
		port = 8080;			

		master = new TPCMaster(2);
		slave1 = new KVServer(10, 5);
		slave2 = new KVServer(10, 5);
		masterserver = new SocketServer(server, port);
		slaveserver1 = new SocketServer(server);
		slaveserver2 = new SocketServer(server);
		handler = new KVClientHandler(master);
		handler1 = new TPCMasterHandler(slave1, 1);
		handler2 = new TPCMasterHandler(slave2, 2);
		new Thread(new runMaster()).start();
		new Thread(new runServers()).start();
		KVClient client = new KVClient(server, port);
		String val = null;
		System.out.println("handleget1");
		
		try {
			Thread.sleep(20000);
			client.put("fuzzy", "wuzzy");
			System.out.println("after put");
			val = client.get("fuzzy");
			System.out.println("after get");
			assertEquals(val, "wuzzy");
			client.del("fuzzy");		//need to clear server
		}
		catch (Exception e){
			System.out.println("Value is: " + val);
			//System.out.println(e.getMsg().getMessage());
			fail("Shouldn't have failed");
		} finally {
			closeServers();
			closeMaster();
		}
	}

	@Test
	public void testHandleGet2() {
		server = "localhost";
		port = 7070;			

		master = new TPCMaster(2);
		slave1 = new KVServer(10, 5);
		slave2 = new KVServer(10, 5);
		masterserver = new SocketServer(server, port);
		slaveserver1 = new SocketServer(server);
		slaveserver2 = new SocketServer(server);
		handler = new KVClientHandler(master);
		handler1 = new TPCMasterHandler(slave1, 1);
		handler2 = new TPCMasterHandler(slave2, 2);
		
		new Thread(new runMaster()).start();
		new Thread(new runServers()).start();
		KVClient client = new KVClient(server, port);	
		System.out.println("handleget2");

		try {
			Thread.sleep(10000);
			//test get on key that doesn't exist
			client.get("supah");
		} catch (InterruptedException e1) {
		} catch (KVException e2){
			String message = e2.getMsg().getMessage();
			System.out.println(message);
			assertTrue(message.equals("Does not exist"));
		} finally {
			closeServers();
			closeMaster();
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