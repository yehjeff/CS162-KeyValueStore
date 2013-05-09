package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.net.InetAddress;

import org.junit.Test;

public class TestTPCMasterHandler{
	//setting up a server, client, slaves...
	
	public void setup() throws Exception {
		SocketServer masterSocket = new SocketServer(InetAddress.getLocalHost().getHostAddress(), 8080);
		TPCMaster tpcMaster = null;
		
		
		
	}
	
	@Test
	public void testPut() {
		
		
		
		
	}

	@Test
	public void testGet() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testDel() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testPhase1Fail() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testPhase2Fail() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testIgnoreNext() {
		fail("Not yet implemented");
	}
}
