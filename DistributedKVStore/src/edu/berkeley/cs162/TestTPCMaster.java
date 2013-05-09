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
	String logPath1;
	String logPath2;
	TPCLog tpcLog1;
	TPCLog tpcLog2;
	
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
		
		logPath1 = 1 + "@" + slaveserver1.getHostname();
		tpcLog1 = new TPCLog(logPath1, slave1);
		logPath2 = 2 + "@" + slaveserver2.getHostname();
		tpcLog2 = new TPCLog(logPath2, slave2);
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
	

	/**@Test
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
		
		try {
			System.out.println("testHandleGet1()");
			Thread.sleep(10000);
			client.put("fuzzy", "wuzzy");
			val = client.get("fuzzy");
			assertEquals(val, "wuzzy");
		}
		catch (Exception e){
			fail("Shouldn't have failed");
		} finally {
			closeServers();
			closeMaster();
		}
	}

	@Test
	public void testHandleGet2() {
		server = "localhost";
		port = 8070;			

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

		try {
			System.out.println("testHandleGet2()");
			Thread.sleep(10000);
			//test get on key that doesn't exist
			client.get("supah");
		} catch (InterruptedException e1) {
		} catch (KVException e2){
			String message = e2.getMsg().getMessage();
			assertTrue(message.equals("Does not exist"));
		} finally {
			closeServers();
			closeMaster();
		}
	}
	
	@Test
	public void testHandleGet3() {
		server = "localhost";
		port = 8060;			

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

		try {
			System.out.println("testHandleGet3()");
			Thread.sleep(10000);
			//test get on key that doesn't exist
			client.get(null);
		} catch (KVException e1){
			assertTrue(e1.getMsg().getMessage().equals("Unknown Error: Key is null or zero-length"));
		} catch (Exception e2) {
		} finally {
			closeServers();
			closeMaster();
		}
	}

	@Test
	public void testHandlePut1() {
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
		String val = null;
		
		new Thread(new runMaster()).start();
		new Thread(new runServers()).start();
		KVClient client = new KVClient(server, port);

		try {
			System.out.println("testHandlePut1()");
			Thread.sleep(10000);
			client.put("hey", "yo");
			val = client.get("hey");
			assertEquals(val, "yo");
			//testing that putting with the same key will overwrite the prev value
			client.put("hey", "boi");
			val = client.get("hey");
			assertEquals(val, "boi");
		} catch (Exception e){
			fail("Shouldn't have failed");
		} finally {
			closeServers();
			closeMaster();
		}
	}

	@Test
	public void testHandlePut2() {
		server = "localhost";
		port = 7060;			

		master = new TPCMaster(2);
		slave1 = new KVServer(10, 5);
		slave2 = new KVServer(10, 5);
		masterserver = new SocketServer(server, port);
		slaveserver1 = new SocketServer(server);
		slaveserver2 = new SocketServer(server);
		handler = new KVClientHandler(master);
		handler1 = new TPCMasterHandler(slave1, 1);
		handler2 = new TPCMasterHandler(slave2, 2);
		String val = null;
		
		new Thread(new runMaster()).start();
		new Thread(new runServers()).start();
		KVClient client = new KVClient(server, port);

		try {
			System.out.println("testHandlePut2()");
			Thread.sleep(10000);
			//testing a bad put with a null key
			client.put(null, "yo");
		} catch (KVException e1){
			System.out.println(e1.getMsg().getMessage());
			assertEquals("Unknown Error: Key is null or zero-length", e1.getMsg().getMessage());
		} catch (Exception e2) {
		} finally {
			closeServers();
			closeMaster();
		}
	}

	@Test
	public void testHandlePut3() {
		server = "localhost";
		port = 7050;			

		master = new TPCMaster(2);
		slave1 = new KVServer(10, 5);
		slave2 = new KVServer(10, 5);
		masterserver = new SocketServer(server, port);
		slaveserver1 = new SocketServer(server);
		slaveserver2 = new SocketServer(server);
		handler = new KVClientHandler(master);
		handler1 = new TPCMasterHandler(slave1, 1);
		handler2 = new TPCMasterHandler(slave2, 2);
		String val = null;
		
		new Thread(new runMaster()).start();
		new Thread(new runServers()).start();
		KVClient client = new KVClient(server, port);

		try {
			System.out.println("testHandlePut3()");
			Thread.sleep(10000);
			//testing a bad put with a null value
			client.put("hey", null);
		} catch (KVException e1){
			System.out.println(e1.getMsg().getMessage());
			assertEquals("Unknown Error: Value is null or zero-length", e1.getMsg().getMessage());
		} catch (Exception e2) {
		} finally {
			closeServers();
			closeMaster();
		}
	}*/
	
	@Test
	public void testHandleDel1() {
		server = "localhost";
		port = 6060;			

		master = new TPCMaster(2);
		slave1 = new KVServer(10, 5);
		slave2 = new KVServer(10, 5);
		masterserver = new SocketServer(server, port);
		slaveserver1 = new SocketServer(server);
		slaveserver2 = new SocketServer(server);
		handler = new KVClientHandler(master);
		handler1 = new TPCMasterHandler(slave1, 1);
		handler2 = new TPCMasterHandler(slave2, 2);
		String val = null;
		
		new Thread(new runMaster()).start();
		new Thread(new runServers()).start();
		KVClient client = new KVClient(server, port);

		try {
			System.out.println("testHandleDel1()");
			Thread.sleep(10000);
			client.put("kame", "hame");
			client.del("kame");
			val = client.get("kame");
			fail();		//never gets here
		} catch (KVException e1) {
			System.out.println(e1.getMsg().getMessage());
			assertEquals("Does not exist", e1.getMsg().getMessage());
		} catch (Exception e2) {
		} finally {
			closeServers();
			closeMaster();
		}
	}

	@Test
	public void testHandleDel2() {
		server = "localhost";
		port = 6050;			

		master = new TPCMaster(2);
		slave1 = new KVServer(10, 5);
		slave2 = new KVServer(10, 5);
		masterserver = new SocketServer(server, port);
		slaveserver1 = new SocketServer(server);
		slaveserver2 = new SocketServer(server);
		handler = new KVClientHandler(master);
		handler1 = new TPCMasterHandler(slave1, 1);
		handler2 = new TPCMasterHandler(slave2, 2);
		String val = null;
		
		new Thread(new runMaster()).start();
		new Thread(new runServers()).start();
		KVClient client = new KVClient(server, port);

		try {
			System.out.println("testHandleDel2()");
			Thread.sleep(10000);
			client.del("poop");
			fail();		//never gets here
		} catch (KVException e1) {
			System.out.println(e1.getMsg().getMessage());
			assertEquals("Does not exist", e1.getMsg().getMessage());
		} catch (Exception e2) {
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
