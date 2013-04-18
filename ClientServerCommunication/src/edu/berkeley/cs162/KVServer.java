/**
 * Slave Server component of a KeyValue store
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

import java.util.concurrent.locks.*;

/**
 * This class defines the slave key value servers. Each individual KVServer 
 * would be a fully functioning Key-Value server. For Project 3, you would 
 * implement this class. For Project 4, you will have a Master Key-Value server 
 * and multiple of these slave Key-Value servers, each of them catering to a 
 * different part of the key namespace.
 *
 */
public class KVServer implements KeyValueInterface {
	private KVStore dataStore = null;
	private KVCache dataCache = null;
	private Lock storeLock;

	private static final int MAX_KEY_SIZE = 256;
	private static final int MAX_VAL_SIZE = 256 * 1024;

	/**
	 * @param numSets number of sets in the data Cache.
	 */
	public KVServer(int numSets, int maxElemsPerSet) {
		dataStore = new KVStore();
		dataCache = new KVCache(numSets, maxElemsPerSet);
		storeLock = new ReentrantLock();

		AutoGrader.registerKVServer(dataStore, dataCache);
	}

	public void put(String key, String value) throws KVException {
		// Must be called before anything else
		AutoGrader.agKVServerPutStarted(key, value);

		// TODO: implement me
		try {

			checkKey(key);
			checkValue(value);

			dataCache.getWriteLock(key).lock();
			dataCache.put(key,value);
			storeLock.lock();
			dataStore.put(key,value);
			storeLock.unlock();
			dataCache.getWriteLock(key).unlock();
		} finally {
			// Must be called before return or abnormal exit
			AutoGrader.agKVServerPutFinished(key, value);
		}
	}

	public String get (String key) throws KVException {
		// Must be called before anything else
		AutoGrader.agKVServerGetStarted(key);

		// TODO: implement me
		try {

			checkKey(key);

			dataCache.getWriteLock(key).lock();
			String valueToReturn = dataCache.get(key);			
			try{
				if (valueToReturn == null) {

					storeLock.lock();
					try {
						valueToReturn = dataStore.get(key);		//May throw an exception if key is not in store
					} finally {
						storeLock.unlock();						//make sure storeLock is unlocked, in case of exception
					}
					dataCache.put(key, valueToReturn);
				}
			}finally {
				dataCache.getWriteLock(key).unlock();			//make sure writeLock is unlocked, in case of exception
			}
			return valueToReturn;
		} finally {
			// Must be called before return or abnormal exit
			AutoGrader.agKVServerGetFinished(key);
		}
		//return null;
	}

	public void del (String key) throws KVException {
		// Must be called before anything else
		AutoGrader.agKVServerDelStarted(key);
		// TODO: implement me
		try {
			
			checkKey(key);
			
			dataCache.getWriteLock(key).lock();
			try {
				storeLock.lock();
				try {
					dataStore.get(key);						// May throw an exception when key does not exist in store
					dataCache.del(key);
					dataStore.del(key);
				} finally {
					storeLock.unlock();						// Make sure storeLock is unlocked, in case of exception
				}
			} finally {
				dataCache.getWriteLock(key).unlock();		// Make sure writeLock is unlocked, in case of exception
			}

		} finally {
			// Must be called before return or abnormal exit
			AutoGrader.agKVServerDelFinished(key);
		}
	}

	/**
	 * Checks the value of the given key and determines if it is valid 
	 * (not null, non-zero length, and not oversized). 
	 * If it is not valid, then the appropriate KVException is thrown.
	 */
	public void checkKey(String key) throws KVException {
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

	/**
	 * Checks the size of the given value and determines if it is valid
	 * (not null, non-zero length, and not oversized).
	 * If it is not valid, then the appropriate KVException is thrown.
	 */
	public void checkValue(String value) throws KVException {
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
