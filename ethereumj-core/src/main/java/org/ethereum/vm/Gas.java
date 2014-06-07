package org.ethereum.vm;

/**
 * The fundamental network cost unit. Paid for exclusively by Ether, which is converted 
 * freely to and from Gas as required. Gas does not exist outside of the internal Ethereum 
 * computation engine; its price is set by the Transaction and miners are free to 
 * ignore Transactions whose Gas price is too low.
 */
public enum Gas {

	G_STEP(1),
	G_STOP(0),
	G_SUICIDE(0),
	G_SLOAD(20),
	G_SHA3(20),
	G_SSTORE(100),
	G_BALANCE(20),
	G_CREATE(100),
	G_CALL(20),					 
	G_MEMORY(1),
	G_TXDATA(5),
	G_TRANSACTION(500);
			 
	private int cost;
	
	private Gas(int value) {
		this.cost = value;
	}
	
	public int cost() {
		return cost;
	}
}