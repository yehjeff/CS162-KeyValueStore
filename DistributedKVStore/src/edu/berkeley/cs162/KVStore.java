/**
 * Persistent Key-Value storage layer. Current implementation is transient, 
 * but assume to be backed on disk when you do your project.
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * This is a dummy KeyValue Store. Ideally this would go to disk, 
 * or some other backing store. For this project, we simulate the disk like 
 * system using a manual delay.
 *
 */
public class KVStore implements KeyValueInterface {
	private Dictionary<String, String> store 	= null;
	
	public KVStore() {
		resetStore();
	}

	private void resetStore() {
		store = new Hashtable<String, String>();
	}
	
	public void put(String key, String value) throws KVException {
		AutoGrader.agStorePutStarted(key, value);
		
		try {
			putDelay();
			store.put(key, value);
		} finally {
			AutoGrader.agStorePutFinished(key, value);
		}
	}
	
	public String get(String key) throws KVException {
		AutoGrader.agStoreGetStarted(key);
		
		try {
			getDelay();
			String retVal = this.store.get(key);
			if (retVal == null) {
			    KVMessage msg = new KVMessage("resp", "Does not exist");
			    throw new KVException(msg);
			}
			return retVal;
		} finally {
			AutoGrader.agStoreGetFinished(key);
		}
	}
	
	public void del(String key) throws KVException {
		AutoGrader.agStoreDelStarted(key);

		try {
			delDelay();
			if(key != null)
				this.store.remove(key);
		} finally {
			AutoGrader.agStoreDelFinished(key);
		}
	}
	
	private void getDelay() {
		AutoGrader.agStoreDelay();
	}
	
	private void putDelay() {
		AutoGrader.agStoreDelay();
	}
	
	private void delDelay() {
		AutoGrader.agStoreDelay();
	}
	
    public String toXML() {
        // TODO: implement me
    	
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			Document doc = builder.newDocument();
			doc.setXmlStandalone(true);
			Element KVStoreElement = doc.createElement("KVStore");
			doc.appendChild(KVStoreElement);
			Enumeration<String> keysEnumerator = this.store.keys();
			for (int i = 0; i < store.size(); i++){
				Element KVPairElement = doc.createElement("KVPair");
				KVStoreElement.appendChild(KVPairElement);
				
				String key = keysEnumerator.nextElement();
				Element keyElement = doc.createElement("Key");
				keyElement.appendChild(doc.createTextNode(key));
				KVPairElement.appendChild(keyElement);
				
				String value = store.get(key);
				Element valueElement = doc.createElement("Value");
				valueElement.appendChild(doc.createTextNode(value));
				KVPairElement.appendChild(valueElement);
				
			}
			
			StringWriter writer = new StringWriter();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			return writer.toString();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

    }        

    public void dumpToFile(String fileName) {
        // TODO: implement me
    	try{
    		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
    		writer.write(this.toXML());
    		writer.close();
    	} catch (Exception e){
    		System.err.println("Could not dump store to file");
    		e.printStackTrace();
    	}
    }

    /**
     * Replaces the contents of the store with the contents of a file
     * written by dumpToFile; the previous contents of the store are lost.
     * @param fileName the file to be read.
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    public void restoreFromFile(String fileName) throws ParserConfigurationException, SAXException, IOException {
        // TODO: implement me
       	
    		File xmlFile = new File(fileName);
    		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    		Document doc = builder.parse(xmlFile);
    		
    		this.resetStore();
    		
    		NodeList KVPairList = doc.getElementsByTagName("KVPair");
    		
    		for (int i = 0; i < KVPairList.getLength(); i++){
    			Element KVPairElement = (Element) KVPairList.item(i);
    			String key = KVPairElement.getElementsByTagName("Key").item(0).getTextContent();
    			String value = KVPairElement.getElementsByTagName("Value").item(0).getTextContent();
    			this.store.put(key, value);
    		}
    	
    	
    }
}
