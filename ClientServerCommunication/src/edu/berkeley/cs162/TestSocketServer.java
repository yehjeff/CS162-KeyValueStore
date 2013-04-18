package edu.berkeley.cs162;

import java.io.IOException;

import org.junit.Test;

import edu.berkeley.cs162.TestKVClient.runServer;

import junit.framework.TestCase;

public class TestSocketServer extends TestCase {

	KVServer key_server = new KVServer(100, 10);
	SocketServer server = new SocketServer("localhost", 8080);
	NetworkHandler handler = new KVClientHandler(key_server);

	private class runServer implements Runnable {
		Thread t;
		runServer() {
			t = new Thread(this);
			t.start();
		}
		public void run() {
			startServer();
		}
	}

	private void startServer() {
		server.addHandler(handler);
		try {
			server.connect();
			server.run();
		}
		catch (IOException e) {
			fail("Network Error");
		}
	}

	private void closeServer() {
		server.stop();
	}

	@Test
	public void testConnectnStop() {
		new runServer();
		closeServer();
	}

}
