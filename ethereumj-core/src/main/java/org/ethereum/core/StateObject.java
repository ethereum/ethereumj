package org.ethereum.core;

import java.math.BigInteger;

import org.ethereum.db.Config;
import org.ethereum.trie.Trie;
import org.ethereum.util.RLP;
import org.ethereum.util.Value;

import static java.util.Arrays.copyOfRange;

public class StateObject {

	// Address of the object
	private byte[] address;
	// Shared attributes
	private BigInteger amount;
	
	private long nonce;
	// Contract related attributes
	private GoState state;
	
	private byte[] init;
	private byte[] body;
	
	// Returns a newly created contract at root
	public static StateObject createContract(byte[] address, BigInteger amount, byte[] root) {
		StateObject contract = new StateObject(address, amount);
		contract.setState(new GoState(new Trie(Config.STATE_DB.getDb(), new String(root))));
		return contract;
	}

	// Returns a newly created account
	public static StateObject createAccount(byte[] address, BigInteger amount) {
		return new StateObject(address, amount);
	}

	public StateObject(byte[] address, BigInteger amount) {
		this.address = address;
		this.amount = amount;
	}
	
	public StateObject(byte[] address, byte[] data) {
		this.address = address;
		this.rlpDecode(data);
	}

	public void setState(GoState state) {
		this.state = state;
	}
	
	public void setBody(byte[] body) {
		this.body = body;
	}
	
	public void setInit(byte[] init) {
		this.init = init;
	}

	public Value getAddress(byte[] address) {
		return new Value(this.state.getTrie().get(new String(address)).getBytes());
	}

	public void setAddress(byte[] address, Object value) {
		this.state.getTrie().update(new String(address), new String(new Value(value).encode()));
	}

	public GoState getState() {
		return this.state;
	}

	public Value getMem(BigInteger num) {
		byte[] nb = num.toByteArray();
		return this.getAddress(nb);
	}

	/**
	 * Get the instruction 
	 *  
	 * @param pc
	 * @return byte wrapped in a Value object
	 */
	public Value getInstr(BigInteger pc) {
		if (this.body.length-1 < pc.longValue()) {
			return new Value(0);
		}
		return new Value( new byte[] { this.body[pc.intValue()] } );
	}

	public void setMem(BigInteger num, Value val) {
		byte[] address = num.toByteArray();
		this.state.getTrie().update(new String(address), new String(val.encode()));
	}

	// Return the gas back to the origin. Used by the Virtual machine or Closures
	public void returnGas(BigInteger gas, BigInteger gasPrice, GoState state) {
		BigInteger remainder = gas.multiply(gasPrice);
		this.addAmount(remainder);
	}
	
	public BigInteger getAmount() {
		return this.amount;
	}

	public void addAmount(BigInteger amount) {
		this.amount = this.amount.add(amount);
	}

	public void subAmount(BigInteger amount) {
		this.amount = this.amount.subtract(amount);
	}

	public void convertGas(BigInteger gas, BigInteger gasPrice) throws RuntimeException {
		BigInteger total = gas.multiply(gasPrice);
		if (total.compareTo(this.amount) > 0) {
			throw new RuntimeException("insufficient amount: " + this.amount + ", " + total);
		}
		this.subAmount(total);
	}

	// Returns the address of the contract/account
	public byte[] getAddress() {
		return this.address;
	}
	
	public long getNonce() {
		return this.nonce;
	}
	
	// Returns the main script body
	public byte[] getBody() {
		return this.body;
	}

	// Returns the initialization script
	public byte[] getInit() {
		return this.init;
	}

	// State object encoding methods
	public byte[] rlpEncode() {
		Object root;
		if (this.state != null) {
			root = this.state.getTrie().getRoot();
		} else {
			root = null;
		}
		return RLP.encode( new Object[] {this.amount, this.nonce, root, this.body});
	}

	public void rlpDecode(byte[] data) {
		Value decoder = new Value(data);

		this.amount = decoder.get(0).asBigInt();
		this.nonce = decoder.get(1).asInt();
		this.state = new GoState(new Trie(Config.STATE_DB.getDb(), decoder.get(2).asObj()));
		this.body = decoder.get(3).asBytes();
	}

	// Converts an transaction in to a state object
	public static StateObject createContract(Transaction tx, GoState state) {
		// Create contract if there's no recipient
		if (tx.isContract()) {
			// FIXME
			byte[] txHash = tx.getHash();
			byte[] contractAddress = copyOfRange(txHash, 12, txHash.length);

			BigInteger value = new BigInteger(1, tx.getValue());
			StateObject contract = StateObject.createContract(contractAddress, value, "".getBytes());
			state.updateStateObject(contract);

			contract.setBody(tx.getData());

			state.updateStateObject(contract);

			return contract;
		}
		return null;
	}	
}