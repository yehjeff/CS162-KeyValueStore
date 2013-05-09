/**
 * Handle TPC connections over a socket interface
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

/**
 * Implements NetworkHandler to handle 2PC operation requests from the Master/
 * Coordinator Server
 *
 */
public class TPCMasterHandler implements NetworkHandler {
	private KVServer kvServer = null;
	private ThreadPool threadpool = null;
	private TPCLog tpcLog = null;

	private long slaveID = -1;

	// Used to handle the "ignoreNext" message
	private boolean ignoreNext = false;

	// States carried from the first to the second phase of a 2PC operation
	private KVMessage originalMessage = null;
	private boolean aborted = true;	

	public TPCMasterHandler(KVServer keyserver) {
		this(keyserver, 1);
	}

	public TPCMasterHandler(KVServer keyserver, long slaveID) {
		this.kvServer = keyserver;
		this.slaveID = slaveID;
		threadpool = new ThreadPool(1);
		
	}

	public TPCMasterHandler(KVServer kvServer, long slaveID, int connections) {
		this.kvServer = kvServer;
		this.slaveID = slaveID;
		threadpool = new ThreadPool(connections);
	}

	private class ClientHandler implements Runnable {
		private KVServer keyserver = null;
		private Socket client = null;

		private void closeConn() {
			try {
				client.close();
			} catch (IOException e) {
			}
		}

		@Override
		public void run() {
			// Receive message from client
			// Implement me
			try {
				KVMessage msg = new KVMessage(client.getInputStream());
				if (tpcLog.hasInterruptedTpcOperation()){
					originalMessage = tpcLog.getInterruptedTpcOperation();
				}
				// Parse the message and do stuff 
				String key = msg.getKey();

				if (msg.getMsgType().equals("putreq")) {
					handlePut(msg, key);
				}
				else if (msg.getMsgType().equals("getreq")) {
					handleGet(msg, key);
				}
				else if (msg.getMsgType().equals("delreq")) {
					handleDel(msg, key);
				} 
				else if (msg.getMsgType().equals("ignoreNext")) {
					// Set ignoreNext to true. PUT and DEL handlers know what to do.
					// Implement me
					ignoreNext = true;

					// Send back an acknowledgment
					// Implement me
					KVMessage ackMsg = new KVMessage("resp","Success");
					ackMsg.sendMessage(client);
				}
				else if (msg.getMsgType().equals("commit") || msg.getMsgType().equals("abort")) {
					// Check in TPCLog for the case when SlaveServer is restarted
					// Implement me
					tpcLog.appendAndFlush(msg);
					handleMasterResponse(msg, originalMessage, aborted);

					// Reset state
					// Implement me
					aborted = false;
					ignoreNext = false;
					originalMessage = null;
				}
				// Finally, close the connection
				closeConn();
			}
			catch (KVException e) {
				// failure here will result in a timeout anyways,
				// no need to send anything back to master
				e.printStackTrace();
			}
			catch (IOException e2) {
				// problem occurred in establishing connection from master
				// to slave. results in timeout, so do nothing here
				e2.printStackTrace();
			}

		}

		private void handlePut(KVMessage msg, String key) throws KVException {
			AutoGrader.agTPCPutStarted(slaveID, msg, key);
			try {
				if (ignoreNext ){
					ignoreNext = false;
					KVMessage abortMsg = new KVMessage("abort");
					abortMsg.sendMessage(client);
				}
				else { 
					try {
						KVServer.checkKey(key);							// better not use this reference to kvserver, cuz autograder might swap out kvserver ?
					} catch (KVException e) {
						KVMessage abortMsg = new KVMessage("abort");
						abortMsg.sendMessage(client);
						return;
					}
					tpcLog.appendAndFlush(msg);
					originalMessage = msg;
					KVMessage readyMsg = new KVMessage("ready");
					readyMsg.sendMessage(client);
				}
			} finally {
				AutoGrader.agTPCPutFinished(slaveID, msg, key);
			}
		}

 		private void handleGet(KVMessage msg, String key) throws KVException {
 			AutoGrader.agGetStarted(slaveID);

 			// Implement me
 			try{
 				// changed to use keyserver, the KVServer given to ClientHandler
 				String value = keyserver.get(key);
 				KVMessage getResp = new KVMessage("resp");
 				getResp.setKey(key);
 				getResp.setValue(value);
 				getResp.sendMessage(client);
 			}
 			catch (KVException e){
 				e.getMsg().sendMessage(client);
 		//		errorMsg.sendMessage(client);
 			}
 			AutoGrader.agGetFinished(slaveID);
 			
		}

		private void handleDel(KVMessage msg, String key) throws KVException {
			AutoGrader.agTPCDelStarted(slaveID, msg, key);
			try {
				if (ignoreNext || !keyserver.hasKey(key)){
					ignoreNext = false;
					aborted = true;
					KVMessage abortMsg = new KVMessage("abort");
					abortMsg.sendMessage(client);
				}
				else{
					try {
						KVServer.checkKey(key);						// better not use this reference to kvserver, cuz autograder might swap out kvserver ?
					} catch (KVException e) {
						KVMessage abortMsg = new KVMessage("abort");
						abortMsg.sendMessage(client);
						return;
					}
					tpcLog.appendAndFlush(msg);
					originalMessage = msg;
					KVMessage readyMsg = new KVMessage("ready");
					readyMsg.sendMessage(client);
				}
			} finally {
				AutoGrader.agTPCDelFinished(slaveID, msg, key);
			}
		}

		/**
		 * Second phase of 2PC
		 * 
		 * @param masterResp Global decision taken by the master
		 * @param origMsg Message from the actual client (received via the coordinator/master)
		 * @param origAborted Did this slave server abort it in the first phase 
		 * @throws KVException 
		 */
		private void handleMasterResponse(KVMessage masterResp, KVMessage origMsg, boolean origAborted) throws KVException {
			AutoGrader.agSecondPhaseStarted(slaveID, origMsg, origAborted);

			if (masterResp.getMsgType().equals("commit")){
				if (originalMessage.getMsgType().equals("putreq")){
					String key = originalMessage.getKey();
					String value = originalMessage.getValue();
					keyserver.put(key, value);
				}
				if (originalMessage.getMsgType().equals("delreq")){
					String key = originalMessage.getKey();
					keyserver.del(key);
				}
			}

			KVMessage ackMsg = new KVMessage("ack");
			ackMsg.sendMessage(client);
			tpcLog.appendAndFlush(ackMsg);

			AutoGrader.agSecondPhaseFinished(slaveID, origMsg, origAborted);
		}

		public ClientHandler(KVServer keyserver, Socket client) {
			this.keyserver = keyserver;
			this.client = client;
		}
	}

	@Override
	public void handle(Socket client) throws IOException {
		AutoGrader.agReceivedTPCRequest(slaveID);
		Runnable r = new ClientHandler(kvServer, client);
		try {
			threadpool.addToQueue(r);
		} catch (InterruptedException e) {
			// TODO: HANDLE ERROR
			return;
		}		
		AutoGrader.agFinishedTPCRequest(slaveID);
	}

	public void stop() {
		threadpool.setShutdownStatus();
	}

	/**
	 * Set TPCLog after it has been rebuilt
	 * @param tpcLog
	 */
	public void setTPCLog(TPCLog tpcLog) {
		this.tpcLog  = tpcLog;
	}

	/**
	 * Registers the slave server with the coordinator
	 * 
	 * @param masterHostName
	 * @param servr KVServer used by this slave server (contains the hostName and a random port)
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws KVException
	 */
	public void registerWithMaster(String masterHostName, SocketServer server) throws UnknownHostException, IOException, KVException {
		AutoGrader.agRegistrationStarted(slaveID);

		Socket master = new Socket(masterHostName, 9090);
		KVMessage regMessage = new KVMessage("register", slaveID + "@" + server.getHostname() + ":" + server.getPort());
		regMessage.sendMessage(master);

		// Receive master response. 
		// Response should always be success, except for Exceptions. Throw away.
		new KVMessage(master.getInputStream());

		master.close();
		AutoGrader.agRegistrationFinished(slaveID);
	}
}
