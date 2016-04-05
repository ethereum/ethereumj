package org.ethereum.util.blockchain;

/**
 * Represents the contract storage which is effectively the
 * mapping( uint256 => uint256 )
 *
 * Created by Anton Nashatyrev on 23.03.2016.
 */
public interface ContractStorage {
    byte[] getStorageSlot(long slot);
    byte[] getStorageSlot(byte[] slot);
}
