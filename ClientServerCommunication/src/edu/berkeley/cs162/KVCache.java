/**
 * Implementation of a set-associative cache.
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

import java.io.StringWriter;
import java.util.LinkedList;

import java.util.concurrent.locks.*;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A set-associate cache which has a fixed maximum number of sets (numSets).
 * Each set has a maximum number of elements (MAX_ELEMS_PER_SET).
 * If a set is full and another entry is added, an entry is dropped based on the eviction policy.
 */
public class KVCache implements KeyValueInterface {	
	private int numSets = 100;
	private int maxElemsPerSet = 10;
	private Entry sets[][];
	LinkedList<Entry> set2CQueues[];
	WriteLock setWriteLocks[];
	/**
	 * Creates a new LRU cache.
	 * @param cacheSize	the maximum number of entries that will be kept in this cache.
	 */
	public KVCache(int numSets, int maxElemsPerSet) {
		this.numSets = numSets;
		this.maxElemsPerSet = maxElemsPerSet;     
		sets = new Entry[numSets][maxElemsPerSet];
		set2CQueues = (LinkedList<Entry>[]) new LinkedList<?>[numSets];	
		setWriteLocks = new WriteLock[numSets];
		for (int i = 0; i < numSets; i++){
			sets[i] = new Entry[maxElemsPerSet];
			set2CQueues[i] = new LinkedList<Entry>();
			setWriteLocks[i] = (new ReentrantReadWriteLock()).writeLock();
			for (int j = 0; j < maxElemsPerSet; j++){
				sets[i][j] = new Entry();
			}
		}

		// TODO: Implement Me!
	}

	public void print2c(String key) {
		int setID = this.getSetId(key);
		for (Entry e : set2CQueues[setID]) {
			System.out.println(e.getKey() + "," + e.getValue() + "," + e.getReferenceBit() + "," + e.isValid());	
		}
		System.out.println();
	}

	/**
	 * Retrieves an entry from the cache.
	 * Assumes the corresponding set has already been locked for writing.
	 * @param key the key whose associated value is to be returned.
	 * @return the value associated to this key, or null if no value with this key exists in the cache.
	 */
	public String get(String key) {
		// Must be called before anything else
		AutoGrader.agCacheGetStarted(key);
		AutoGrader.agCacheGetDelay();
		try{
			String valueToReturn = null;
			// TODO: Implement Me!
			int setId = this.getSetId(key);
			for (int i = 0; i < this.maxElemsPerSet; i++){
				Entry entry = this.sets[setId][i];
				if (entry.isValid() && entry.getKey().equals(key)){
					entry.turnOnReferenceBit();
					valueToReturn = entry.getValue();			
				}
			}
			return valueToReturn;

		} finally {

			// Must be called before returning
			AutoGrader.agCacheGetFinished(key);
		}
	}

	/**
	 * Adds an entry to this cache.
	 * If an entry with the specified key already exists in the cache, it is replaced by the new entry.
	 * If the cache is full, an entry is removed from the cache based on the eviction policy
	 * Assumes the corresponding set has already been locked for writing.
	 * @param key	the key with which the specified value is to be associated.
	 * @param value	a value to be associated with the specified key.
	 * @return true is something has been overwritten 
	 */
	public void put(String key, String value) {
		// Must be called before anything else
		AutoGrader.agCachePutStarted(key, value);
		AutoGrader.agCachePutDelay();

		// TODO: Implement Me!
		try { 
			int setId = this.getSetId(key);

			for (int i = 0; i < this.maxElemsPerSet; i++){
				Entry entry = this.sets[setId][i];
				if (entry.isValid() && entry.getKey().equals(key)){
					entry.setValue(value);
					entry.turnOffReferenceBit();
					this.set2CQueues[setId].remove(entry);
					this.set2CQueues[setId].addLast(entry);
					return;
				}
			}

			for (int i = 0; i < this.maxElemsPerSet; i++){
				Entry entry = this.sets[setId][i];
				if (!entry.isValid()){
					entry.setValue(value);
					entry.setKey(key);
					entry.turnOffReferenceBit();
					entry.turnOnValidBit();
					set2CQueues[setId].addLast(entry);
					return;
				}
			}

			Entry entry = set2CQueues[setId].removeFirst();
			while (entry.getReferenceBit() && entry.isValid()){
				entry.turnOffReferenceBit();
				set2CQueues[setId].addLast(entry);
				entry = set2CQueues[setId].removeFirst();
			}
			entry.setValue(value);
			entry.setKey(key);
			entry.turnOffReferenceBit();
			entry.turnOnValidBit();
			set2CQueues[setId].addLast(entry);
			return;
		} finally {
			AutoGrader.agCachePutFinished(key, value);
		}
		// Must be called before returning
	}

	/**
	 * Removes an entry from this cache.
	 * Assumes the corresponding set has already been locked for writing.
	 * @param key	the key with which the specified value is to be associated.
	 */
	public void del (String key) {
		// Must be called before anything else
		AutoGrader.agCacheDelStarted(key);
		AutoGrader.agCacheDelDelay();

		// TODO: Implement Me!
		try{
			int setId = this.getSetId(key);
			for (int i = 0; i < this.maxElemsPerSet; i++){
				Entry entry = this.sets[setId][i];
				if (entry.isValid() && entry.getKey().equals(key)) {
					entry.turnOffValidBit();
					this.set2CQueues[setId].remove(entry);
				}
			} 
		} finally {
			// Must be called before returning
			AutoGrader.agCacheDelFinished(key);
		}
	}

	/**
	 * @param key
	 * @return	the write lock of the set that contains key.
	 */
	public WriteLock getWriteLock(String key) {
		// TODO: Implement Me!
		int setId = this.getSetId(key);
		return this.setWriteLocks[setId];
	}

	/**
	 * 
	 * @param key
	 * @return	set of the key
	 */
	private int getSetId(String key) {
		return Math.abs(key.hashCode()) % numSets;
	}

	public String toXML() {
		// TODO: Implement Me!
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document doc = builder.newDocument();
			doc.setXmlStandalone(true);
			Element KVCacheElement = doc.createElement("KVCache");
			doc.appendChild(KVCacheElement);
			for (int i = 0; i < this.numSets; i++){
				Element setElement = doc.createElement("Set");
				setElement.setAttribute("Id", "" + i);
				KVCacheElement.appendChild(setElement);
				for (int j = 0; j < this.maxElemsPerSet; j++){
					Entry entry = this.sets[i][j];

					boolean isValid = entry.isValid();
					boolean isReferenced = entry.getReferenceBit();


					Element cacheEntryElement = doc.createElement("CacheEntry");
					cacheEntryElement.setAttribute("isReferenced", ""+isReferenced);
					cacheEntryElement.setAttribute("isValid", ""+isValid);

					String key = entry.getKey();
					Element keyElement = doc.createElement("Key");
					keyElement.appendChild(doc.createTextNode(key));
					cacheEntryElement.appendChild(keyElement);

					String value = entry.getValue();
					Element valueElement = doc.createElement("Value");
					valueElement.appendChild(doc.createTextNode(value));
					cacheEntryElement.appendChild(valueElement);

					setElement.appendChild(cacheEntryElement);


				}

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
		}


		return null;
	}

	private class Entry{
		private String key;
		private String value;
		private boolean referenceBit;
		private boolean valid;

		public Entry(){
			this.valid = false;
			this.key = "";
			this.value = "";
		}

		public String getKey(){
			return this.key;
		}

		public void setKey(String key){
			this.key = key;
		}

		public String getValue(){
			return this.value;
		}

		public void setValue(String value){
			this.value = value;
		}

		public boolean getReferenceBit(){
			return this.referenceBit;
		}

		public void turnOnReferenceBit(){
			this.referenceBit = true;
		}

		public void turnOffReferenceBit(){
			this.referenceBit = false;
		}

		public boolean isValid(){
			return this.valid;
		}

		public void turnOnValidBit(){
			this.valid = true;
		}

		public void turnOffValidBit(){
			this.valid = false;
		}


	}
}
