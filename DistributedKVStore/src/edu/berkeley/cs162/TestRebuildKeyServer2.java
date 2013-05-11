package edu.berkeley.cs162;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import junit.framework.TestCase;


public class TestRebuildKeyServer2 extends TestCase {

	HashMap<Long,TPCLog> tpcLogMap = new HashMap<Long,TPCLog>();
	HashMap<Long,SocketServer> socketServerMap = new HashMap<Long,SocketServer>();
	HashMap<Long,KVServer> kvServerMap = new HashMap<Long,KVServer>();
	HashMap<Long,TPCMasterHandler> tpcMasterHandlerMap = new HashMap<Long,TPCMasterHandler>();
	TPCMaster tpcMaster;
	SocketServer masterSocketServer;
	String masterHostname = null;



	public void runMaster(){
		try {
			tpcMaster = new TPCMaster(1);
			tpcMaster.run();

			System.out.println("Binding Master:");
			masterSocketServer = new SocketServer(InetAddress.getLocalHost().getHostAddress(), 8080);
			NetworkHandler handler = new KVClientHandler(tpcMaster);
			masterSocketServer.addHandler(handler);
			masterSocketServer.connect();
			System.out.println("Starting Master");
			masterSocketServer.run();		
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	public void runSlave(long slaveID){
		try {
			System.out.println("Binding SlaveServer:");
			KVServer keyServer = new KVServer(100, 10);
			SocketServer server;
			kvServerMap.put(slaveID, keyServer);

			server = new SocketServer(InetAddress.getLocalHost().getHostAddress());
			socketServerMap.put(slaveID, server);

			TPCMasterHandler handler = new TPCMasterHandler(keyServer, slaveID);
			tpcMasterHandlerMap.put(slaveID, handler);
			server.addHandler(handler);
			server.connect();

			String logPath = slaveID + "@" + server.getHostname();
			TPCLog tpcLog = new TPCLog(logPath, keyServer);
			tpcLogMap.put(slaveID, tpcLog);

			tpcLog.rebuildKeyServer();

			handler.setTPCLog(tpcLog);

			handler.registerWithMaster(masterHostname, server);

			System.out.println("Starting SlaveServer at " + server.getHostname() + ":" + server.getPort());
			server.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopMaster() {

	}

	public void stopSlave(long id){

	}

/*
	public void testRebuild(){
		try{			




			masterHostname = InetAddress.getLocalHost().getHostAddress();
			Thread masterThread = new Thread(new Runnable() { public void run() { runMaster(); } } );
			Thread slaveThread1 = new Thread(new Runnable() { public void run() { runSlave(1); } } );
			Thread slaveThread2 = new Thread(new Runnable() { public void run() { runSlave(2); } } );
			masterThread.start();
			slaveThread1.start(); slaveThread2.start();
			Thread.sleep(2000);


			KVClient client = new KVClient(masterHostname,8080);
			String key = "key1";
			String value = "value1";
			try {
				client.put("key1", "value1");
				value = client.get("key1");
				assertTrue(value.equals("value1"));
				client.put("key2", "value2");
				value = client.get("key2");
				assertTrue(value.equals("value2"));
			
				client.put("key5", "value5");
				value = client.get("key5");
				assertTrue(value.equals("value5"));
				client.del("key5");
			} catch (KVException e){
				e.printStackTrace();
				fail("bad exception thrown:" + e.getMsg().getMessage());
			}


			System.out.println("\n\nSTOPPING BOTH SLAVE SERVERS\n\n");
			// a good way to stop a slave server?
			slaveThread1.stop();
			slaveThread2.stop();
			slaveThread1 = new Thread(new Runnable() { public void run() { runSlave(1); } } );
			slaveThread2 = new Thread(new Runnable() { public void run() { runSlave(2); } } );
			slaveThread1.start(); slaveThread2.start();
			Thread.sleep(2000);
			client = new KVClient(masterHostname,8080);
			try {
				value = client.get("key1");
				assertTrue(value.equals("value1"));
				value = client.get("key2");
				assertTrue(value.equals("value2"));
			
			} catch (KVException e){
				e.printStackTrace();
				fail("bad exception thrown:" + e.getMsg().getMessage());
			}
			try {
				value = client.get("key5");
				fail("key5 should have been deleted");
			} catch (KVException e){
				assertTrue(e.getMsg().getMessage().equals("Does not exist"));
			}

		//	masterThread.stop();
			slaveThread1.stop();slaveThread2.stop();
			Thread.sleep(2000);


		}catch (Exception e){
			e.printStackTrace();
		} finally {

		}
	}
*/

	public void testRebuildFailInOperation(){
		try{			
			masterHostname = InetAddress.getLocalHost().getHostAddress();
			Thread masterThread = new Thread(new Runnable() { public void run() { runMaster(); } } );
			final Thread slaveThread1 = new Thread(new Runnable() { public void run() { runSlave(1); } } );
			final Thread slaveThread2 = new Thread(new Runnable() { public void run() { runSlave(2); } } );
			masterThread.start();
			slaveThread1.start(); slaveThread2.start();
			Thread.sleep(2000);


			KVClient client = new KVClient(masterHostname,8080);
			String key = "key1";
			String value = "value1";
			int numLoops = 10;
			for (int i = 0; i < numLoops; i++){
				try {
					System.out.println("Loop " + i);
					new Thread(new Runnable() { public void run() { stopInMilliseconds(slaveThread1,  new Thread(new Runnable() { public void run() { runSlave(1); } } ), 100); } } ).start();
					client.put("key1", "value1");
					value = client.get("key1");
					assertTrue(value.equals("value1"));

					client.put("key2", "value2");
					value = client.get("key2");
					assertTrue(value.equals("value2"));
					client.del("key2");
				} catch (KVException e){
					e.printStackTrace();
					fail("bad exception thrown:" + e.getMsg().getMessage());
				} 
				try {
					value = client.get("key2");
					fail("should have Does not exist");
				} catch (KVException e){
					assertTrue(e.getMsg().getMessage().equals("Does not exist"));
				}
			}


			//masterThread.stop();
			slaveThread1.stop();slaveThread2.stop();
			Thread.sleep(2000);

		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void stopInMilliseconds(Thread threadOld, Thread threadNew, long t){
		try {
			Thread.sleep(t);
		}catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("\tKilling Slave Server");
		threadOld.stop();
		threadNew.start();
	}
}
