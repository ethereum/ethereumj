package org.ethereum.vm;

/**
 * The fundamental network cost unit. Paid for exclusively by Ether, which is converted 
 * freely to and from Gas as required. Gas does not exist outside of the internal Ethereum 
 * computation engine; its price is set by the Transaction and miners are free to 
 * ignore Transactions whose Gas price is too low.
 */
public class GasCost {

	public static int STEP = 1;
	public static int STOP = 0;
	public static int SUICIDE = 0;
	public static int SLOAD = 20;
	public static int SHA3 = 20;
	public static int SSTORE = 100;
	public static int BALANCE = 20;
	public static int CREATE = 100;
	public static int CALL = 20;			 
	public static int MEMORY = 1;
	public static int TXDATA = 5;
	public static int TRANSACTION = 500;
}