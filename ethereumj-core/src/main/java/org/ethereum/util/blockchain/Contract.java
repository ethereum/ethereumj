package org.ethereum.util.blockchain;

/**
 * Abstract Ethereum contract
 *
 * Created by Anton Nashatyrev on 01.04.2016.
 */
public interface Contract {

    /**
     * Address of the contract. If the contract creation transaction is
     * still in pending state (not included to a block) the address can be missed
     */
    byte[] getAddress();

    /**
     * Submits contract invocation transaction
     */
    void call(byte[] callData);

    /**
     * Returns the interface for accessing contract storage
     */
    ContractStorage getStorage();

    /**
     * Returns the contract code binary Hex encoded
     */
    String getBinary();
}
