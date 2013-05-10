package edu.berkeley.cs162;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import junit.framework.TestCase;

public class TestRebuildKeyServer extends TestCase {
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
				e.printStackTrace();
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
				e.printStackTrace();
			}
		}
	}
	public class runServers implements Runnable {
	
		public void run() {
			startServers();
		}
		
	}
	public class runServer1 implements Runnable {
		public void run() {
			restartServer1();
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
		} catch (KVException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Starting SlaveServer at " + slaveserver1.getHostname() + ":" + slaveserver1.getPort());
		new Thread(new runSlaveServer(1)).start();
		

		System.out.println("Starting SlaveServer at " + slaveserver2.getHostname() + ":" + slaveserver2.getPort());
		new Thread(new runSlaveServer(2)).start();
		
	}
	
	public void restartServer1() {
		slaveserver1 = new SocketServer("localhost");
		slaveserver1.addHandler(handler1);
		try {
			slaveserver1.connect();
			tpcLog1.rebuildKeyServer();
			handler1.setTPCLog(tpcLog1);
			handler1.registerWithMaster(server, slaveserver1);


		} catch (KVException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		System.out.println("ReStarting SlaveServer at " + slaveserver1.getHostname() + ":" + slaveserver1.getPort());
		new Thread(new runSlaveServer(1)).start();
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
			e.printStackTrace();

		}
	}
	
	public void closeServer1() {
		System.out.println("Closing server1");
		slaveserver1.stop();
		handler1.stop();
		try {
		//Thread.sleep(10000);
		} catch (Exception e){
			e.printStackTrace();

		}
	}

	@Test
	public void testRebuild1() {
		//normal server shutdown
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
			Thread.sleep(2000);
			client.put("keyA", "valA");
			client.put("keyB", "valB");
			client.put("keyDel", "delMe");
			client.put("keyC", "valC");
			client.del("keyDel");
			client.put("keyD", "valD");
			client.put("keyE", "valE");
			val = client.get("keyA");
			assertEquals("get should find correct key from recovered slave", "valA", val);
			//System.out.println("ASuccess");
			val = client.get("keyB");
			assertEquals("get should find correct key from recovered slave", "valB", val);
			//System.out.println("BSuccess");
			val = client.get("keyC");
			assertEquals("get should find correct key from recovered slave", "valC", val);
			//System.out.println("CSuccess");
			val = client.get("keyD");
			//System.out.println("DSuccess");
		}
		catch (Exception e){
			fail("Shouldn't have failed with the put");
		} finally {
			//close both servers
			closeServers();
			//restart one of the servers
			new Thread(new runServer1()).start();
			try {
				Thread.sleep(10000);
				//get should find the correct key from the log of the one restarted server
				client.put("keyA", "valA");
				System.out.println("APutted");
				client.put("keyB", "valB");
				System.out.println("BPutted");
				client.put("keyDel", "delMe");
				System.out.println("DelPutted");
				client.put("keyC", "valC");
				System.out.println("CPutted");
				client.del("keyDel");
				System.out.println("DelDel'd");
				client.put("keyD", "valD");
				System.out.println("DPutted");
				client.put("keyE", "valE");
				System.out.println("EPutted");
				val = client.get("keyA");
				assertEquals("get should find correct key from recovered slave", "valA", val);
				System.out.println("ASuccess");
				val = client.get("keyB");
				assertEquals("get should find correct key from recovered slave", "valB", val);
				System.out.println("BSuccess");
				val = client.get("keyC");
				assertEquals("get should find correct key from recovered slave", "valC", val);
				System.out.println("CSuccess");
				val = client.get("keyD");
				System.out.println("DSuccess");
				assertEquals("get should find correct key from recovered slave", "valD", val);
				val = client.get("keyDel");
				//assertNull("the deleted key should stay deleted", val);
			}
			catch (KVException KVe) {
				if (!KVe.getMsg().getMessage().equals("Does not exist")) {
					fail(KVe.getMsg().getMessage());
				}
			}
			catch (Exception e){
				fail("Shouldn't have failed on get of a valid key after restarting a slave server");
			}
			finally {
				closeServer1();
				closeMaster();
			}
		}
	}

	

}
