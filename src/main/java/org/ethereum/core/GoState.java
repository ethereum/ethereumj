package org.ethereum.core;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.ethereum.trie.Trie;

public class GoState {

	// States within the ethereum protocol are used to store anything
	// within the merkle trie. States take care of caching and storing
	// nested states. It's the general query interface to retrieve:
	// * Contracts
	// * Accounts

	// The trie for this structure
	private Trie trie;
	// Nested states
	private Map<String, GoState> states;

	// Create a new state from a given trie
	public GoState(Trie trie) {
		this.trie = trie;
		states = new HashMap<String, GoState>();
	}
	
	public void add(String key, GoState state) {
		this.states.put(key, state);
	}

	// Resets the trie and all siblings
	public void reset() {
		this.trie.undo();

		// Reset all nested states
		for (GoState state : states.values()) {
			state.reset();
		}
	}

	// Syncs the trie and all siblings
	public void sync() {
		this.trie.sync();

		// Sync all nested states
		for (GoState state : states.values()) {
			state.sync();
		}
	}

	// Purges the current trie.
	public int purge() {
		return this.trie.getIterator().purge();
	}

	public StateObject getContract(byte[] address) {
		String data = this.trie.get(new String(address));
		if (data == "") {
			return null;
		}

		// build contract
		StateObject contract = new StateObject(address, data.getBytes());

		// Check if there's a cached state for this contract
		GoState cachedState = this.states.get(new String(address));
		if (cachedState != null) {
			contract.setState( cachedState );
		} else {
			// If it isn't cached, cache the state
			this.states.put(new String(address), contract.getState());
		}

		return contract;
	}

	public StateObject getAccount(byte[] address) {
		String data = this.trie.get(new String(address));
		if (data == "") {
			return StateObject.createAccount(address, BigInteger.ZERO);
		} else {
			return new StateObject(address, data.getBytes());
		}
	}

	public boolean cmp(GoState other) {
		return this.trie.cmp(other.getTrie());
	}

	public GoState copy() {
		return new GoState(this.trie.copy());
	}

//	type ObjType byte
//
//	enum (
//		NullTy ObjType = iota,
//		AccountTy,
//		ContractTy,
//		UnknownTy
//	)

	// Returns the object stored at key and the type stored at key
	// Returns null if nothing is stored
//	public (*ethutil.Value, ObjType) getStateObject(byte[] key) {
//		
//		// Fetch data from the trie
//		String data = this.trie.get(new String(key));
//		// Returns the null type, indicating nothing could be retrieved.
//		// Anything using this function should check for this ret val
//		if (data == "") {
//			return (null, NullTy)
//		}
//
//		var enum ObjType
//		Value val = new Value(data.getBytes());
//		// Check the length of the retrieved value.
//		// Len 2 = Account
//		// Len 3 = Contract
//		// Other = invalid for now. If other types emerge, add them here
//		if (val.length() == 2) {
//			typ = AccountTy
//		} else if (val.length == 3) {
//			typ = ContractTy
//		} else {
//			typ = UnknownTy
//		}
//
//		return (val, typ);
//	}

	// Updates any given state object
	public void updateStateObject(StateObject stateObject) {
		byte[] addr = stateObject.getAddress();

		if (stateObject.getState() != null) {
			this.states.put(new String(addr), stateObject.getState());
		}

		this.trie.update(new String(addr), new String(stateObject.rlpEncode()));
	}

	public void put(byte[] key, byte[] object) {
		this.trie.update(new String(key), new String(object));
	}

	/**
	 * Instead of calling this method, call state.getTrie().getRoot()
	 * @return
	 */
	@Deprecated()
	public Object getRoot() {
		return this.trie.getRoot();
	}
	
	public Trie getTrie() {
		return this.trie;
	}
}