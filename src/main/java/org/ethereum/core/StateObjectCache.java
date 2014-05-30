package org.ethereum.core;

import java.util.HashMap;
import java.util.Map;

public class StateObjectCache {

	// The cached state and state object cache are helpers which will give you somewhat
	// control over the nonce. When creating new transactions you're interested in the 'next'
	// nonce rather than the current nonce. This to avoid creating invalid-nonce transactions.
	Map<String, CachedStateObject> cachedObjects;
	
	public StateObjectCache() {
		this.cachedObjects = new HashMap<String, CachedStateObject>();
	}
	
	public CachedStateObject add(byte[] address, StateObject stateObject) {
		CachedStateObject state = new CachedStateObject(stateObject.getNonce(), stateObject);
		this.cachedObjects.put(new String(address), state);
		return state;
	}
	
	public CachedStateObject get(byte[] address)  {
		return this.cachedObjects.get(new String(address));
	}
	
	public class CachedStateObject {
		private long nonce;
		private StateObject stateObject;
		
		public CachedStateObject(long nonce, StateObject stateObject) {
			this.nonce = nonce;
			this.stateObject = stateObject;			
		}
	}
}