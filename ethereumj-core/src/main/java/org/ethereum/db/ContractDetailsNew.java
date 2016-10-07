package org.ethereum.db;

import org.ethereum.core.AccountState;
import org.ethereum.trie.Trie;
import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class ContractDetailsNew implements ContractDetails {

    AccountState accountState;

    Trie storageTrie;

    byte[] address;
    boolean deleted;

    public ContractDetailsNew(AccountState accountState, Trie storageTrie) {
        this.accountState = accountState;
        this.storageTrie = storageTrie;
    }

    @Override
    public void put(DataWord key, DataWord value) {
        if (value.equals(DataWord.ZERO)) {
            storageTrie.delete(key.getData());
        } else {
            storageTrie.update(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
        }
    }

    @Override
    public DataWord get(DataWord key) {
        DataWord result = null;

        byte[] data = storageTrie.get(key.getData());
        if (data.length > 0) {
            byte[] dataDecoded = RLP.decode2(data).get(0).getRLPData();
            result = new DataWord(dataDecoded);
        }

        return result;
    }

    @Override
    public byte[] getCode() {
        return storageTrie.get(accountState.getCodeHash());
    }

    @Override
    public void setCode(byte[] code) {
        byte[] codeHash = sha3(code);
        storageTrie.update(codeHash, code);
        accountState.setCodeHash(codeHash);
    }

    @Override
    public void syncStorage() {
        storageTrie.sync();
        accountState.setStateRoot(storageTrie.getRootHash());
    }

    @Override
    public byte[] getStorageHash() {
        return storageTrie.getRootHash();
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public byte[] getAddress() {
        return address;
    }

    @Override
    public void setAddress(byte[] address) {
        this.address = address;
    }


    /***** TO REMOVE  *********/

    @Override
    public ContractDetails getSnapshotTo(byte[] hash) {
        return null;
    }

    @Override
    public Map<DataWord, DataWord> getStorage(@Nullable Collection<DataWord> keys) {
        return null;
    }

    @Override
    public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {

    }

    @Override
    public void setStorage(Map<DataWord, DataWord> storage) {

    }

    @Override
    public byte[] getCode(byte[] codeHash) {
        return new byte[0];
    }

    @Override
    public void decode(byte[] rlpCode) {

    }

    @Override
    public void setDirty(boolean dirty) {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }

    @Override
    public int getStorageSize() {
        return 0;
    }

    @Override
    public Set<DataWord> getStorageKeys() {
        return null;
    }

    @Override
    public Map<DataWord, DataWord> getStorage() {
        return null;
    }

    @Override
    public ContractDetails clone() {
        return null;
    }
}

