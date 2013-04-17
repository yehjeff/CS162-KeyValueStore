package edu.berkeley.cs162;

import java.io.IOException;

import org.junit.Test;

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
	
	

	@Test
	public void testKVClient() {
		// trivial, done above
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
		socketserver.stop();
	}

	@Test
	public void testPut() {
		
		startServer();
		
		KVClient client = new KVClient(server, port);
		
		try {
			client.put("fuzzy", "wuzzy");
		}
		catch (KVException e){
			System.out.println(e.getMsg().getMessage());
		}
	}

	@Test
	public void testGet() {
		closeServer();
		startServer();
		
		KVClient client = new KVClient(server, port);
		
		try {
			client.get("fuzzy");
		}
		catch (KVException e){
			System.out.println(e.getMsg().getMessage());
		}
	}

	@Test
	public void testDel() {
		closeServer();
		startServer();
		
		KVClient client = new KVClient(server, port);
		
		try {
			client.del("fuzzy");
		}
		catch (KVException e){
			System.out.println(e.getMsg().getMessage());
		}
	}

}
