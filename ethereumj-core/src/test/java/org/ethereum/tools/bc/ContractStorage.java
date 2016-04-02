package org.ethereum.tools.bc;

/**
 * Created by Anton Nashatyrev on 23.03.2016.
 */
public interface ContractStorage {
    byte[] getStorageSlot(long slot);
    byte[] getStorageSlot(byte[] slot);
}
