package edu.berkeley.cs162;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Test;

import junit.framework.TestCase;

public class TestKVClientHandler extends TestCase {

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
	public void testHandle() {
		new runServer();
		KVClient client = new KVClient("localhost", 8080);
		Socket sock;
		try {
			sock = new Socket("localhost", 8080);
			handler.handle(sock);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			fail("FAILED handle");
		}
		closeServer();
	}
	
}
