package org.ethereum.util.blockchain;

import org.ethereum.vm.DataWord;

/**
 * Created by Arsalan on 2017-04-21.
 */
class SolidityStorageImpl implements SolidityStorage {
    byte[] contractAddr;
    StandaloneBlockchain standaloneBlockchain;

    public SolidityStorageImpl(byte[] contractAddr) {
        this.contractAddr = contractAddr;
    }

    @Override
    public byte[] getStorageSlot(long slot) {
        return getStorageSlot(new DataWord(slot).getData());
    }

    @Override
    public byte[] getStorageSlot(byte[] slot) {
        DataWord ret = standaloneBlockchain.getBlockchain().getRepository().getContractDetails(contractAddr).get(new DataWord(slot));
        return ret.getData();
    }
}
