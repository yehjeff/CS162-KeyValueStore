/**
 * Master for Two-Phase Commits
 * 
 * @author Mosharaf Chowdhury (http://www.mosharaf.com)
 * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
 *
 * Copyright (c) 2012, University of California at Berkeley
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of University of California, Berkeley nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs162;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TPCMaster {

	/**
	 * Implements NetworkHandler to handle registration requests from 
	 * SlaveServers.
	 * 
	 */
	private class TPCRegistrationHandler implements NetworkHandler {

		private ThreadPool threadpool = null;


		public TPCRegistrationHandler() {
			// Call the other constructor
			this(1);	
		}

		public TPCRegistrationHandler(int connections) {
			threadpool = new ThreadPool(connections);	
		}

		@Override
		public void handle(Socket client) throws IOException {    
			try {
				threadpool.addToQueue(new RegistrationHandler(client));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private class RegistrationHandler implements Runnable {

			private Socket client = null;

			public RegistrationHandler(Socket client) {
				this.client = client;
			}

			@Override
			public void run() {    
				try {
					KVMessage registerMsg = new KVMessage(client.getInputStream());
					isParseable(registerMsg);     //throws KVException if not parsable
					String delims = "[@]";
					String[] tokens = registerMsg.getMessage().split(delims);
					Long slaveID = Long.parseLong(tokens[0]);

					if (registerMsg.getMsgType().equals("register")) {
						slaveServerIDs.add(slaveID);
						SlaveInfo newEntry = new SlaveInfo(registerMsg.getMessage());
						slaveInfoMap.put(slaveID, newEntry);
					}
					System.out.println("Successfully registered " + registerMsg.getMessage());
					KVMessage ackMsg = new KVMessage("resp", "Successfully registered " + registerMsg.getMessage());
					ackMsg.sendMessage(client);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						client.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub

		}	
	}

	/**
	 *  Data structure to maintain information about SlaveServers
	 *
	 */
	private class SlaveInfo {
		// 64-bit globally unique ID of the SlaveServer
		private long slaveID = -1;
		// Name of the host this SlaveServer is running on
		private String hostName = null;
		// Port which SlaveServer is listening to
		private int port = -1;

		/**
		 * 
		 * @param slaveInfo as "SlaveServerID@HostName:Port"
		 * @throws KVException
		 */
		public SlaveInfo(String slaveInfo) throws KVException {  	
			// implement me  
			String delims = "[@:]"; //delim by @ or :
			String[] tokens = slaveInfo.split(delims);
			slaveID = Long.parseLong(tokens[0]);
			hostName = tokens[1];
			port = Integer.parseInt(tokens[2]);
		}

		public long getSlaveID() {
			return slaveID;
		}

		public Socket connectHost() throws KVException {
			// TODO: Optional Implement Me!    
			try {
				Socket sock = new Socket(this.hostName, this.port);		//should it be this.server????
				return sock;
			} catch (UnknownHostException u) {
				KVMessage exceptMsg = new KVMessage("resp");
				exceptMsg.setMessage("Network Error: Could not connect");
				throw new KVException(exceptMsg);
			} catch (IOException e) {
				KVMessage exceptMsg = new KVMessage("resp");
				exceptMsg.setMessage("Network Error: Could not create socket");
				throw new KVException(exceptMsg);
			}
			//return null;
		}

		public void closeHost(Socket sock) throws KVException {
			// TODO: Optional Implement Me!
			if (sock == null) {
				KVMessage exceptMsg = new KVMessage("resp");
				exceptMsg.setMessage("Unknown Error: no socket given");
				throw new KVException(exceptMsg);
			}
			try {
				sock.close();
			} catch (IOException e) {
				KVMessage exceptMsg = new KVMessage("resp");
				exceptMsg.setMessage("Unknown Error: could not close connection");
				throw new KVException(exceptMsg);
			}
		}
	}

	// Timeout value used during 2PC operations
	private static final int TIMEOUT_MILLISECONDS = 5000;

	// Cache stored in the Master/Coordinator Server
	private KVCache masterCache = new KVCache(100, 10); //100, 10

	// Registration server that uses TPCRegistrationHandler
	private SocketServer regServer = null;

	// Number of slave servers in the system
	private int numSlaves = -1;

	// ID of the next 2PC operation
	private Long tpcOpId = 0L;

	private TreeSet<Long> slaveServerIDs;
	private HashMap<Long, SlaveInfo> slaveInfoMap;

	private static final int MAX_KEY_SIZE = 256;
	private static final int MAX_VAL_SIZE = 256 * 1024;
	/**
	 * Creates TPCMaster
	 * 
	 * @param numSlaves number of expected slave servers to register
	 * @throws Exception
	 */
	public TPCMaster(int numSlaves) {
		// Using SlaveInfos from command line just to get the expected number of SlaveServers 
		this.numSlaves = numSlaves;

		// Create registration server
		regServer = new SocketServer("localhost", 9090);

		this.slaveInfoMap = new HashMap<Long,SlaveInfo>();
		this.slaveServerIDs = new TreeSet<Long>();

	}

	/**
	 * Calculates tpcOpId to be used for an operation. In this implementation
	 * it is a long variable that increases by one for each 2PC operation. 
	 * 
	 * @return 
	 */
	private String getNextTpcOpId() {
		tpcOpId++;
		return tpcOpId.toString();		
	}

	/**
	 * Start registration server in a separate thread
	 */
	public void run() {
		AutoGrader.agTPCMasterStarted();
		try {
			// implement me
			regServer.connect();
			regServer.addHandler(new TPCRegistrationHandler(numSlaves));
			Thread regThread = new Thread(new Runnable(){
				public void run() {
					try {
						regServer.run();
					} catch (IOException e){
						e.printStackTrace();
					}
				}
			});
			regThread.start();
		} catch (IOException e){
			e.printStackTrace();
		}
		AutoGrader.agTPCMasterFinished();
	}

	/**
	 * Converts Strings to 64-bit longs
	 * Borrowed from http://stackoverflow.com/questions/1660501/what-is-a-good-64bit-hash-function-in-java-for-textual-strings
	 * Adapted from String.hashCode()
	 * @param string String to hash to 64-bit
	 * @return
	 */
	private long hashTo64bit(String string) {
		// Take a large prime
		long h = 1125899906842597L; 
		int len = string.length();

		for (int i = 0; i < len; i++) {
			h = 31*h + string.charAt(i);
		}
		return h;
	}

	/**
	 * Compares two longs as if they were unsigned (Java doesn't have unsigned data types except for char)
	 * Borrowed from http://www.javamex.com/java_equivalents/unsigned_arithmetic.shtml
	 * @param n1 First long
	 * @param n2 Second long
	 * @return is unsigned n1 less than unsigned n2
	 */
	private boolean isLessThanUnsigned(long n1, long n2) {
		return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
	}

	private boolean isLessThanEqualUnsigned(long n1, long n2) {
		return isLessThanUnsigned(n1, n2) || n1 == n2;
	}	

	/**
	 * Find first/primary replica location
	 * @param key
	 * @return
	 */
	private SlaveInfo findFirstReplica(String key) {
		// 64-bit hash of the key
		long hashedKey = hashTo64bit(key.toString());
		Long firstReplicaID = slaveServerIDs.ceiling(hashedKey);
		if (firstReplicaID == null){
			firstReplicaID = slaveServerIDs.ceiling(Long.MIN_VALUE);
		}
		return slaveInfoMap.get(firstReplicaID);
	}

	/**
	 * Find the successor of firstReplica to put the second replica
	 * @param firstReplica
	 * @return
	 */
	private SlaveInfo findSuccessor(SlaveInfo firstReplica) {
		// implement me
		Long successorID = slaveServerIDs.higher(firstReplica.getSlaveID());
		if (successorID == null){
			successorID = slaveServerIDs.ceiling(Long.MIN_VALUE);
		}
		return slaveInfoMap.get(successorID);
	}

	/**
	 * Synchronized method to perform 2PC operations one after another
	 * You will need to remove the synchronized declaration if you wish to attempt the extra credit
	 * 
	 * @param msg
	 * @param isPutReq
	 * @throws KVException
	 */
	public synchronized void performTPCOperation(KVMessage msg, boolean isPutReq) throws KVException {
		AutoGrader.agPerformTPCOperationStarted(isPutReq);
		try {
			// implement me
			String key = msg.getKey();
			checkKey(key);			// better not use this reference to kvserver, cuz autograder might swap out kvserver ?
			String value = null;
			if (isPutReq){
				value = msg.getValue();
				checkValue(value);
			}

			SlaveInfo info1 = findFirstReplica(key);
			SlaveInfo info2 = findSuccessor(info1);
			Long id1 = info1.getSlaveID();
			Long id2 = info2.getSlaveID();

			Socket slaveSocket1, slaveSocket2;
			KVMessage exceptionMsg;
			String currentTPCOpId = getNextTpcOpId();
			msg.setTpcOpId(currentTPCOpId);
			masterCache.getWriteLock(key).lock();

			try{
				/* PHASE 1 */
				slaveSocket1 = info1.connectHost();
				slaveSocket2 = info2.connectHost();
				slaveSocket1.setSoTimeout(TIMEOUT_MILLISECONDS);
				slaveSocket2.setSoTimeout(TIMEOUT_MILLISECONDS);
				msg.sendMessage(slaveSocket1);
				msg.sendMessage(slaveSocket2);
				KVMessage responseMsg1, responseMsg2;
				try{
					responseMsg1 = new KVMessage(slaveSocket1.getInputStream());
					responseMsg2 = new KVMessage(slaveSocket2.getInputStream());
				} catch (KVException e){
					/* PHASE 2 - ABORT due to timeout */
					info1.closeHost(slaveSocket1);
					info2.closeHost(slaveSocket2);
					KVMessage commitMsg = new KVMessage("abort");
					sendDecision(id1,commitMsg);
					sendDecision(id2,commitMsg);
					throw e;
				}

				if (responseMsg1.getMsgType().equals("ready") && responseMsg2.getMsgType().equals("ready")){
					/* PHASE 2 - SUCCESS */
					KVMessage commitMsg = new KVMessage("commit");
					sendDecision(id1, commitMsg);
					sendDecision(id2, commitMsg);
					if (isPutReq){
						masterCache.put(key,value);
					} else {
						masterCache.del(key);
					}
				} else {
					/* PHASE 2 - ABORT because a slave voted abort */
					KVMessage abortMsg = new KVMessage("abort");
					sendDecision(id1, abortMsg);
					sendDecision(id2, abortMsg);
				
					//exceptionMsg = new KVMessage ("resp",abortMsg.getMessage());
					if (responseMsg1.getMsgType().equals("abort")) {
						exceptionMsg = new KVMessage ("resp", responseMsg1.getMessage());
					} else {
						exceptionMsg = new KVMessage ("resp", responseMsg2.getMessage());
					}
					throw new KVException(exceptionMsg);
				}
				info1.closeHost(slaveSocket1);
				info2.closeHost(slaveSocket2);
			} finally {
				masterCache.getWriteLock(key).unlock();
			}
		} catch (KVException e) {
			throw e;
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			AutoGrader.agPerformTPCOperationFinished(isPutReq);
		}
	}

	private void sendDecision(Long slaveID, KVMessage decision){
		KVMessage ackMsg;
		try {
			while (true){
				SlaveInfo info = slaveInfoMap.get(slaveID);
				Socket slaveSocket = info.connectHost();
				slaveSocket.setSoTimeout(TIMEOUT_MILLISECONDS);
				decision.sendMessage(slaveSocket);
				try {
					ackMsg = new KVMessage(slaveSocket.getInputStream());
				} catch (KVException e) {
					info.closeHost(slaveSocket);
					continue;
				}

				if (ackMsg.getMsgType().equals("ack"))
					return;
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Perform GET operation in the following manner:
	 * - Try to GET from first/primary replica
	 * - If primary succeeded, return Value
	 * - If primary failed, try to GET from the other replica
	 * - If secondary succeeded, return Value
	 * - If secondary failed, return KVExceptions from both replicas
	 * 
	 * @param msg Message containing Key to get
	 * @return Value corresponding to the Key
	 * @throws KVException
	 */
	public String handleGet(KVMessage msg) throws KVException {
		AutoGrader.aghandleGetStarted();
		try {
			// implement me
			Socket slaveSocket;
			String key, value;
			SlaveInfo info1, info2;
			key = msg.getKey();
			checkKey(key);
			KVMessage responseMsg, exceptionMsg;
			info1 = findFirstReplica(key);
			masterCache.getWriteLock(key).lock();
			try {
				value = masterCache.get(key);
				if (value != null)
					return value;

				slaveSocket = info1.connectHost();
				slaveSocket.setSoTimeout(TIMEOUT_MILLISECONDS);
				msg.sendMessage(slaveSocket);
				responseMsg = new KVMessage(slaveSocket.getInputStream());
				if (responseMsg.getMessage() != null && responseMsg.getMessage().equals("Does not exist")){
					exceptionMsg = new KVMessage("resp", "Does not exist");
					throw new KVException(exceptionMsg);
				}
				value = responseMsg.getValue();
				masterCache.put(key,value);
				info1.closeHost(slaveSocket);
				return value;
			}catch (KVException e1){
				if (e1.getMsg().getMessage() != null && e1.getMsg().getMessage().equals("Does not exist")){
					exceptionMsg = new KVMessage("resp", "Does not exist");
					throw new KVException(exceptionMsg);
				}
				key = msg.getKey();
				info2 = findSuccessor(info1);
				try {
					slaveSocket = info2.connectHost();

					slaveSocket.setSoTimeout(TIMEOUT_MILLISECONDS);
					msg.sendMessage(slaveSocket);

					responseMsg = new KVMessage(slaveSocket.getInputStream());
					if (responseMsg.getMessage() != null && responseMsg.getMessage().equals("Does not exist")){
						exceptionMsg = new KVMessage("resp", "Does not exist");
						throw new KVException(exceptionMsg);
					}
					value = responseMsg.getValue();
					masterCache.put(key, value);
					return value;
				} catch (KVException e2){
					if (e2.getMsg().getMessage() != null && e2.getMsg().getMessage().equals("Does not exist")){
						exceptionMsg = new KVMessage("resp", "Does not exist");
						throw new KVException(exceptionMsg);
					}
					exceptionMsg = new KVMessage("resp","@" + info1.getSlaveID() + ":=" + e1.getMsg().getMessage() + "@" + info2.getSlaveID() + "\n:=" + e2.getMsg().getMessage());
					throw new KVException(exceptionMsg);
				}
			}finally {
				masterCache.getWriteLock(key).unlock();
			}
		} catch (KVException e) {
			throw e;
		}catch (Exception e){
			e.printStackTrace();
		} finally {
			AutoGrader.aghandleGetFinished();
		}
		return null;
	}

	/**
	 * DOES PARSING STUFF
	 * 
	 * @param msg Registration message to parse
	 * @throws KVException
	 */
	public static void isParseable(KVMessage msg) throws KVException {
		String regMsg = msg.getMessage();
		System.out.println(msg.getMessage());
		//String validIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
		//String validHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
		String pattern = "-?[0-9]+@[A-Za-z0-9\\.]+:[0-9]+";
		//String pattern = "[0-9]+@("+ validIpAddressRegex + "|" + validHostnameRegex+"):[0-9]+";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(regMsg);
		if (!m.matches()) {
			KVMessage exceptMsg = new KVMessage("resp");
			exceptMsg.setMessage("Registration Error: Received unparseable slave information");
			throw new KVException(exceptMsg);
		}
	}

	public void stop(){
		regServer.stop();
	}


	public static void checkKey(String key) throws KVException {
		KVMessage exceptMsg = new KVMessage("resp");
		if (key == null) {
			exceptMsg.setMessage("Unknown Error: Null Key");
			throw new KVException(exceptMsg);
		}
		if (key.length() > MAX_KEY_SIZE) {
			exceptMsg.setMessage("Oversized key");
			throw new KVException(exceptMsg);
		}
		if (key.length() < 1) {
			exceptMsg.setMessage("Unknown Error: Zero Size Key");
			throw new KVException(exceptMsg);
		}
	}

	public static void checkValue(String value) throws KVException {
		KVMessage exceptMsg = new KVMessage("resp");
		if (value == null) {
			exceptMsg.setMessage("Unknown Error: Null Value");
			throw new KVException(exceptMsg);
		}
		if (value.length() > MAX_VAL_SIZE) {
			exceptMsg.setMessage("Oversized value");
			throw new KVException(exceptMsg);
		}
		if (value.length() < 1) {
			exceptMsg.setMessage("Unknown Error: Zero Size Value");
			throw new KVException(exceptMsg);
		}
	}

}
