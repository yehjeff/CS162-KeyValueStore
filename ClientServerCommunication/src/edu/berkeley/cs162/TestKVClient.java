package edu.berkeley.cs162;

import java.io.IOException;

import org.junit.Test;
import java.util.*;



import junit.framework.TestCase;

public class TestKVClient extends TestCase {
	
	String server = "localhost";		// no idea wtf to put here...
	int port = 8080;			
	
	KVServer key_server = new KVServer(100, 10);
	SocketServer socketserver = new SocketServer("localhost", 8080);
	NetworkHandler handler = new KVClientHandler(key_server);
		
	/*
	KVClient client = new KVClient(server, port);
	KVServer kvserver = new KVServer(2, 2);
	
	SocketServer ss = new SocketServer(server, port);
	KVClientHandler ch = new KVClientHandler(kvserver);
	*/
	
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
		socketserver.addHandler(handler);
		try {
			socketserver.connect();
			System.out.println("Starting Server");
			socketserver.run();
		}
		catch (IOException e) {
			System.out.println("Network Error");
		}
	}
	
	public void closeServer() {
		System.out.println("closing server");
		socketserver.stop();
	}
	
	@Test
	public void testClient() {
		new runServer();
		
		KVClient client = new KVClient(server, port);	
		try {
			System.out.println("testing put");
			client.put("fuzzy", "wuzzy");
			System.out.println("put ok");
			
			System.out.println("putting (fuzzy, wuzzy) (again)");
			client.put("fuzzy", "wuzzy");
			System.out.println("ok");
			
			System.out.println("putting (key2, value2)");
			client.put("key2", "value2");
			System.out.println("ok");
			
			System.out.println("getting key=fuzzy");			
			String value = client.get("fuzzy");					
			System.out.println("returned: " + value);
			
			System.out.println("getting key=key2");			
			String value2 = client.get("key2");					
			System.out.println("returned: " + value2);
			
			System.out.println("deleting key=key2");			
			client.del("key2");					
			System.out.println("ok");
			
			System.out.println("deleting key=key2 (again)");			
			client.del("key2");					
			System.out.println("ok");
			
			closeServer();
		}
		catch (KVException e){
			System.out.println(e.getMsg().getMessage());
		}
		
		
	}
	
	// Below tests work individually, but won't serialize properly. instead combined into singluar test function
	
	/**
	@Test
	public void testPut() {
		new runServer();
		
		KVClient client = new KVClient(server, port);	
		try {
			System.out.println("testing put");
			client.put("fuzzy", "wuzzy");
			System.out.println("ok");
			closeServer();
		}
		catch (KVException e){
			System.out.println(e.getMsg().getMessage());
		}
	}

	
	@Test
	public void testGet() {
		new runServer();
		
		KVClient client = new KVClient(server, port);
		
		try {
			System.out.println("testing get");
			client.get("fuzzy");
			System.out.println("ok");
			closeServer();

		}
		catch (KVException e){
			System.out.println(e.getMsg().getMessage());
		}
	}

	

	
	@Test
	public void testDel() {
		new runServer();
		
		KVClient client = new KVClient(server, port);
		
		try {
			System.out.println("testing del");
			client.del("fuzzy");
			System.out.println("ok");
			//closeServer();

		}
		catch (KVException e){
			System.out.println(e.getMsg().getMessage());
		}
	}
	*/
}
