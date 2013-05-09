package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import edu.berkeley.cs162.TestTPCMaster.runMaster;
import edu.berkeley.cs162.TestTPCMaster.runServers;
import edu.berkeley.cs162.TestTPCMaster.runSlaveServer;

public class TestTPCMasterHandler{
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
//				tpcLog1.rebuildKeyServer();
				handler1.setTPCLog(tpcLog1);
				slaveserver1.connect();
				handler1.registerWithMaster(server, slaveserver1);

//				tpcLog2.rebuildKeyServer();
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
	public void testIgnoreNext() {
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
		System.out.println("ignoreNext Test");

		try {
			Thread.sleep(10000);
			//insert ignoreNext, then try a put. should abort
			client.put("blink", "dagger");
			System.out.println("Testing get on (blink, dagger), should pass");

			System.out.println("injecting ignoreNext, then del on blink");
			client.ignoreNext(server, slaveserver1.getPort());
			client.del("blink");
			
			System.out.println("trying to get blink, should succeed");
			client.get("blink");
			System.out.println("Get on blinkdagger passed (again)");
			
			System.out.println("injecting ignoreNext, then get on fak");
			client.ignoreNext(server, slaveserver1.getPort());
			client.put("fak", "you");
				
				// Should abort...
				// try a get, should fail
				
			client.get("fak");
				
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
	
}
