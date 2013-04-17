/**
 * Client component for generating load for the KeyValue store. 
 * This is also used by the Master server to reach the slave nodes.
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

//import java.net.Socket;
import java.net.*;
import java.io.*;


/**
 * This class is used to communicate with (appropriately marshalling and unmarshalling) 
 * objects implementing the {@link KeyValueInterface}.
 *
 * @param <K> Java Generic type for the Key
 * @param <V> Java Generic type for the Value
 */
public class KVClient implements KeyValueInterface {

	private String server = null;
	private int port = 0;
	
	/**
	 * @param server is the DNS reference to the Key-Value server
	 * @param port is the port on which the Key-Value server is listening
	 * 
	 * Filled in, no tests written yet
	 */
	public KVClient(String server, int port) {
		this.server = server;
		this.port = port;
	}
	
	private Socket connectHost() throws KVException {
		try {
			Socket sock = new Socket(this.server, this.port);
			return sock;
		}
		catch (UnknownHostException u){
				KVMessage exceptMsg = new KVMessage("resp");
				exceptMsg.setMessage("Network Error: Could not connect");
				throw new KVException(exceptMsg);
			}
		catch (IOException e){
				KVMessage exceptMsg = new KVMessage("resp");
				exceptMsg.setMessage("Network Error: Could not create socket");
				throw new KVException(exceptMsg);
			}
	}

	
	private void closeHost(Socket sock) throws KVException {
		if (sock == null){
			KVMessage exceptMsg = new KVMessage("resp");
			exceptMsg.setMessage("Unknown Error: no socket given");
			throw new KVException(exceptMsg);
		}
		try {
			sock.close();
			}
		catch (IOException e){
			KVMessage exceptMsg = new KVMessage("resp");
			exceptMsg.setMessage("Unknown Error: could not close connection");
			throw new KVException(exceptMsg);
		}

	}
	
	public void put(String key, String value) throws KVException {
		try{
			Socket sock = connectHost();
			KVMessage msg = new KVMessage("putreq");
			msg.setKey(key);
			msg.setValue(value);
			msg.sendMessage(sock);
			InputStream inputStream = sock.getInputStream(); 
			KVMessage responseMsg = new KVMessage(inputStream);
			closeHost(sock);
			KVMessage exceptMsg = new KVMessage("resp");
			exceptMsg.setMessage(responseMsg.getMessage());
			
			// Throw an exception if the command wasn't successful
			// changed to use .equals()
			if (!exceptMsg.getMessage().equals("Success") ){
			
				throw new KVException(exceptMsg);
			}
		}
		catch (IOException f){
			KVMessage exceptMsg = new KVMessage("resp");
			exceptMsg.setMessage("Network Error: could not send data");
			throw new KVException(exceptMsg);
		}
		catch (KVException e){
			throw e;
		}
	}

	public String get(String key) throws KVException {
		try{
			Socket sock = connectHost();
			KVMessage msg = new KVMessage("getreq");
			msg.setKey(key);
			msg.sendMessage(sock);
			InputStream inputStream = sock.getInputStream();
			KVMessage responseMsg = new KVMessage(inputStream);
			closeHost(sock);
			KVMessage exceptMsg = new KVMessage("resp");
			exceptMsg.setMessage(responseMsg.getMessage());
			
			if (responseMsg.getValue() == null) {
				throw new KVException(exceptMsg);
			}
			else return responseMsg.getValue();
			
		}
		catch (IOException f){
			KVMessage exceptMsg = new KVMessage("resp");
			exceptMsg.setMessage("Network Error: could not recieve data");
			throw new KVException(exceptMsg);
		}
		catch (KVException e){
			throw e;
		}
	}
	
	public void del(String key) throws KVException {
		try{
			Socket sock = connectHost();
			KVMessage msg = new KVMessage("delreq");
			msg.setKey(key);
			msg.sendMessage(sock);
			InputStream inputStream = sock.getInputStream();         
			KVMessage responseMsg = new KVMessage(inputStream);
			closeHost(sock);
			KVMessage exceptMsg = new KVMessage("resp");
			exceptMsg.setMessage(responseMsg.getMessage());
			
			// Throw an exception if the command wasn't successful
			if (!exceptMsg.getMessage().equals("Success") ){
				throw new KVException(exceptMsg);
			}
		}
		catch (IOException f){
			KVMessage exceptMsg = new KVMessage("resp");
			exceptMsg.setMessage("Network Error: could not send data");
			throw new KVException(exceptMsg);
		}
		catch (KVException e){
			throw e;
		}
	}	
}
