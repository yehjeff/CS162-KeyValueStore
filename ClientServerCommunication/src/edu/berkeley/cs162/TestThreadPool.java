package edu.berkeley.cs162;

import java.net.Socket;

import org.junit.Test;

import junit.framework.TestCase;

public class TestThreadPool extends TestCase {

	//ThreadPool tp = new ThreadPool(1);	
	
	@Test
	public void testThreadPool() {
		// Create a new threadpool, after creation threads automatically start running, so

		ThreadPool tp = new ThreadPool(1);

		assertFalse(tp.getShutdownStatus());
		//assertTrue(true);
	}

	@Test
	public void testAddToQueue() {
		ThreadPool tp = new ThreadPool(1);
		
		KVServer kvServer = new KVServer(2, 2);
		Socket client = new Socket();
		
		class ClientHandler implements Runnable {
			private KVServer kvServer = null;
			private Socket client = null;
			
			public void run() {
				// dummy
			}
			
			public ClientHandler(KVServer kvServer, Socket client) {
				this.kvServer = kvServer;
				this.client = client;
			}
		}
		
		Runnable r = new ClientHandler(kvServer, client); 
		try {
			tp.addToQueue(r);
		}
		catch (InterruptedException e){
			// ignore this for now?
		}
		assertEquals(1, tp.jobs.size());
		assertFalse(tp.jobs.isEmpty());
		assertEquals(r, tp.jobs.getLast());
		//fail("Not yet implemented");
	}

	@Test
	public void testGetJob() {
		fail("Not yet implemented");
	}

}
