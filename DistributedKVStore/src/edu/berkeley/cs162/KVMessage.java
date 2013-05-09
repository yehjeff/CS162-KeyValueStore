/**
 * XML Parsing library for the key-value store
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * This is the object that is used to generate messages the XML based messages 
 * for communication between clients and servers.
 */
public class KVMessage implements Serializable{
	private String msgType = null;
	private String key = null;
	private String value = null;
	private String message = null;
    private String tpcOpId = null; 
	
	public final String getKey() {
		return key;
	}

	public final void setKey(String key) {
		this.key = key;
	}

	public final String getValue() {
		return value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final String getMessage() {
		return message;
	}

	public final void setMessage(String message) {
		this.message = message;
	}

	public String getMsgType() {
		return msgType;
	}

	public String getTpcOpId() {
		return tpcOpId;
	}

	public void setTpcOpId(String tpcOpId) {
		this.tpcOpId = tpcOpId;
	}

	/* Solution from http://weblogs.java.net/blog/kohsuke/archive/2005/07/socket_xml_pitf.html */
	private class NoCloseInputStream extends FilterInputStream {
	    public NoCloseInputStream(InputStream in) {
	        super(in);
	    }
	  
	    public void close() {
	    } // ignore close
	}
	
	//TODO: new types abort/ready/commit/ack DONE?
	//TODO: add catch for inputstream timeout and throw the right kvexception 
	//TODO: input stream constructor needs to be able to read TPCOpID DONE?
	//TODO: toXML needs to work with new msgTypes as well as the TPCOpID field DONE?
	
	/***
	 * 
	 * @param msgType
	 * @throws KVException of type "resp" with message "Message format incorrect" if msgType is unknown
	 */
	public KVMessage(String msgType) throws KVException {
		//first check for valid msgType where valid = getreq, putreq. delreq, or resp
	    if (msgType != null && ( msgType.equals("getreq") || (msgType.equals("putreq")) || (msgType.equals("delreq")) || (msgType.equals("resp")) || (msgType.equals("abort")) || (msgType.equals("ready")) || (msgType.equals("commit")) || (msgType.equals("ack")) || (msgType.equals("register")) || (msgType.equals("ignoreNext")))) {
	    	//passed msgType checking
	    	this.msgType = msgType;
	    } else {
	    	//unknown or incorrectly formatted msgType
	    	KVMessage exceptMsg = new KVMessage("resp", "Message format incorrect");
	    	throw new KVException(exceptMsg);
	    }
	}
	
	public KVMessage(String msgType, String message) throws KVException {
		//first check for valid msgType where valid = getreq, putreq. delreq, or resp
	    if (message != null && msgType != null && ((msgType.equals("getreq")) || (msgType.equals("putreq")) || (msgType.equals("delreq")) || (msgType.equals("resp")) || (msgType.equals("abort")) || (msgType.equals("ready")) || (msgType.equals("commit")) || (msgType.equals("ack")) || (msgType.equals("register")) || (msgType.equals("ignoreNext")))) {
	    	//passed msgType checking
	    	this.msgType = msgType;
	    	this.message = message;
	    } else {
	    	//unknown or incorrectly formatted msgType
	    	KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Message format incorrect");
	    	throw new KVException(exceptMsg);
	    }
	}
	
	 /***
     * Parse KVMessage from incoming network connection
     * @param sock
     * @throws KVException if there is an error in parsing the message. The exception should be of type "resp and message should be :
     * a. "XML Error: Received unparseable message" - if the received message is not valid XML.
     * b. "Network Error: Could not receive data" - if there is a network error causing an incomplete parsing of the message.
     * c. "Message format incorrect" - if there message does not conform to the required specifications. Examples include incorrect message type. 
     */
	
	//this one does no error checking, that is done later in toXML
	public KVMessage(InputStream input) throws KVException {
	     try {
	    	 DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    	//do the parsing now
	    	 Document doc = builder.parse(new NoCloseInputStream(input));
	    	 //finds the first (and theoretically only) KVMessage tag
	    	 Node KVmsg = doc.getElementsByTagName("KVMessage").item(0);
	    	 //casts the node as an element (which extends node) to access attributes.
	    	 if (KVmsg == null) {
	    		 KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: No 'KVMessage' Tag");
		    	 throw new KVException(exceptMsg);
	    	 }
	    	 Element KVElement = (Element) KVmsg;
	    	 //gets the msgType from KVMessage's attribute field
	    	 this.msgType = KVElement.getAttribute("type");
	    	 //find the known element nodes inside the KVMessage
	    	 Node keyNode =  KVElement.getElementsByTagName("Key").item(0); 
	    	 Node valueNode =  KVElement.getElementsByTagName("Value").item(0);
	    	 Node messageNode =  KVElement.getElementsByTagName("Message").item(0);
	    	 Node tpcNode = KVElement.getElementsByTagName("TPCOpId").item(0);
	    	 //make sure the found element node is not null (i.e. the KVMessage didn't have that tag) before setting it
	    	 //self-note: NodeList.item(x) returns null if x >= NodeList.getLength()
	    	 if (keyNode != null) {
	    		 this.key = keyNode.getTextContent();
	    	 }
	    	 if (valueNode != null) {
	    		 this.value = valueNode.getTextContent();
	    	 }
	    	 if (messageNode != null) {
	    		 this.message = messageNode.getTextContent();
	    	 }
	    	 if (tpcNode != null) {
	    		 this.tpcOpId = tpcNode.getTextContent();
	    	 }
	    	 // NOTE: do we need to close the input stream? the TA didn't mention needing to
	    	 
	     } catch (SocketTimeoutException TOErr) {
	    	 KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Timed Out");
	    	 throw new KVException(exceptMsg);
	     } catch (IOException IOErr) {
	    	 KVMessage exceptMsg = new KVMessage("resp", "Network Error: Could not receive data");
	    	 throw new KVException(exceptMsg);
	     } catch (SAXException SAXErr) {
	    	 //parsing error
	    	 KVMessage exceptMsg = new KVMessage("resp", "XML Error: Received unparseable message");
	    	 throw new KVException(exceptMsg);
	     } catch (IllegalArgumentException IllArgErr) {
	    	 //if input stream is null this gets thrown by parse
	    	 KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: NULL Input Stream");
	    	 throw new KVException(exceptMsg);
	     } catch (ParserConfigurationException ParsConfErr) {
	    	 //document builder cannot be created with requested configuration
	    	 KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Document builder cannot be created with requested configuration");
	    	 throw new KVException(exceptMsg);
	     }
	}
	
	/**
	 * 
	 * @param sock Socket to receive from
	 * @throws KVException if there is an error in parsing the message. The exception should be of type "resp and message should be :
	 * a. "XML Error: Received unparseable message" - if the received message is not valid XML.
	 * b. "Network Error: Could not receive data" - if there is a network error causing an incomplete parsing of the message.
	 * c. "Message format incorrect" - if there message does not conform to the required specifications. Examples include incorrect message type. 
	 * @throws IOException 
	 */
	public KVMessage(Socket sock) throws KVException, IOException {
		//our implementation does not need this. just felt like tossing this in.
		this(sock.getInputStream());
	}

	/**
	 * 
	 * @param sock Socket to receive from
	 * @param timeout Give up after timeout milliseconds
	 * @throws KVException if there is an error in parsing the message. The exception should be of type "resp and message should be :
	 * a. "XML Error: Received unparseable message" - if the received message is not valid XML.
	 * b. "Network Error: Could not receive data" - if there is a network error causing an incomplete parsing of the message.
	 * c. "Message format incorrect" - if there message does not conform to the required specifications. Examples include incorrect message type. 
	 */
	public KVMessage(Socket sock, int timeout) throws KVException {
	     // TODO: implement me
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param kvm
	 */
	public KVMessage(KVMessage kvm) {
		this.msgType = kvm.msgType;
		this.key = kvm.key;
		this.value = kvm.value;
		this.message = kvm.message;
		this.tpcOpId = kvm.tpcOpId;
	}

	
	/**
	 * Generate the XML representation for this message.
	 * @return the XML String
	 * @throws KVException if not enough data is available to generate a valid KV XML message
	 */
	public String toXML() throws KVException {
		try {
			//we want to add key/value/message depending on the msgType
			boolean includeKey = false;
			boolean includeVal = false;
			boolean includeMsg = false;
			
			//confirm that msgType is not null
			if (this.msgType == null){
				KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: No Message Type");
				throw new KVException(exceptMsg);
			}
			//confirm for different msgTypes
			if (this.msgType.equals("getreq")) {
				includeKey = true;
			} else if (this.msgType.equals("putreq")) {
				includeKey = true;
				includeVal = true;
			} else if (this.msgType.equals("delreq")) {
				includeKey = true;
			} else if (this.msgType.equals("resp")) {
				//we control this, so if not null return it!
				if (this.key != null) {
					includeKey = true;
				}
				if (this.value != null) {
					includeVal = true;
				}
				if (this.message != null) {
					includeMsg = true;
				}
				// or if it doesnt satisfy only msg or only key/value then complain
				if (!(((includeKey == true) && (includeVal == true) && (includeMsg == false)) || ((includeKey == false) && (includeVal == false) && (includeMsg == true)))) {
					KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Incorrect fields for 'resp' msgType");
					throw new KVException(exceptMsg);
				}
			} else if (this.msgType.equals("commit")) {
				//we control this so checking doesn't need to be as strict
				//do nothing, TPCOpID is included for all msgTypes
			} else if (this.msgType.equals("ready")) {
				//we control this so checking doesn't need to be as strict
				//do nothing, TPCOpID is included for all msgTypes
			} else if (this.msgType.equals("abort")) {
				//we control this so checking doesn't need to be as strict
				//TPCOpID is included for all msgTypes
				//some aborts include an Error Message
				if (this.message != null) {
					includeMsg = true;
				}
			} else if (this.msgType.equals("ack")) {
				//we control this so checking doesn't need to be as strict
				//do nothing, TPCOpID is included for all msgTypes
				
			} else if (this.msgType.equals("register")) {
				includeMsg = true;
			} else if (this.msgType.equals("ignoreNext")) {
				//do nothing, ignoreNext does not come with additional information
		    } else {
		    	//unknown or incorrectly formatted msgType
		    	KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Message format incorrect");
		    	throw new KVException(exceptMsg);
		    }
			
			//Now that we know what we want in the XML, we can construct it
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			//make top level KVMessage tag
			Element rootKVMessage = doc.createElement("KVMessage");
			doc.appendChild(rootKVMessage);
			//add the attribute field 'type' to KVMessage
			rootKVMessage.setAttribute("type", this.msgType);
			
			//use the booleans above to append the necessary information
			if (includeKey == true) {
				if ((this.key == null) || (this.key.length() == 0)) {
					KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Key is null or zero-length");
			    	throw new KVException(exceptMsg);
				}
				Element keyElem = doc.createElement("Key");
				keyElem.appendChild(doc.createTextNode(this.key));
				rootKVMessage.appendChild(keyElem);
			}
			if (includeVal == true) {
				if ((this.value == null) || (this.value.length() == 0)) {
					KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Value is null or zero-length");
			    	throw new KVException(exceptMsg);
				}
				Element valElem = doc.createElement("Value");
				valElem.appendChild(doc.createTextNode(this.value));
				rootKVMessage.appendChild(valElem);
			}
			if (includeMsg == true) {
				if ((this.message == null) || (this.message.length() == 0)) {
					KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Message is null or zero-length");
			    	throw new KVException(exceptMsg);
				}
				Element msgElem = doc.createElement("Message");
				msgElem.appendChild(doc.createTextNode(this.message));
				rootKVMessage.appendChild(msgElem);
			}
			//added check for tpcOpId
			if (this.tpcOpId != null) {
				if (this.tpcOpId.length() == 0) {
					KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: TPCOpId is zero-length");
					throw new KVException(exceptMsg);
				}
				Element tpcElem = doc.createElement("TPCOpId");
				tpcElem.appendChild(doc.createTextNode(this.tpcOpId));
				rootKVMessage.appendChild(tpcElem);
			}
			
			//use StringWriter and Transformer to convert the created XML document to string format
			StringWriter writer = new StringWriter();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			doc.setXmlStandalone(true);
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			return writer.toString();
		} catch (ParserConfigurationException e) {
			//document builder cannot be created with requested configuration
	    	KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Document builder cannot be created with requested configuration");
	    	throw new KVException(exceptMsg);
		} catch (TransformerException e) {
			//transformer error
			KVMessage exceptMsg = new KVMessage("resp", "Unknown Error: Transformer could not be created or transform interrupted");
	    	throw new KVException(exceptMsg);
		}
 
	}
	
	public void sendMessage(Socket sock) throws KVException {
		try {
	      	String outputMsg = this.toXML();
			OutputStream outStream = sock.getOutputStream();
	      	byte[] toSend = outputMsg.getBytes();
	      	outStream.write(toSend);
	      	outStream.flush();
	      	sock.shutdownOutput();
		} catch (IOException IOErr) {
			KVMessage exceptMsg = new KVMessage("resp", "Network Error: Could not send data");
			throw new KVException(exceptMsg);
		} catch (KVException KVErr) {
			throw KVErr;
		}
	}
	public void sendMessage(Socket sock, int timeout) throws KVException {
		/*
		 * As was pointed out, setting a timeout when sending the message (while would still technically work),
		 * is a bit silly. As such, this method will be taken out at the end of Spring 2013.
		 */
		// TODO: optional implement me
	}
}

