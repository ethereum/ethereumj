package org.ethereum.jsontestsuite.suite;

import org.ethereum.db.ContractDetails;
import org.ethereum.trie.SecureTrie;
import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;

import javax.xml.crypto.Data;
import java.util.*;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Roman Mandeleil
 * @since 24.06.2014
 */
public class ContractDetailsCacheImpl extends AbstractContractDetails {

    private Map<DataWord, DataWord> storage = new HashMap<>();

    ContractDetails origContract;

    public ContractDetailsCacheImpl(ContractDetails origContract) {
        this.origContract = origContract;
        if (origContract != null) {
            if (origContract instanceof AbstractContractDetails) {
                setCodes(((AbstractContractDetails) this.origContract).getCodes());
            } else {
                setCode(origContract.getCode());
            }
        }
    }

    @Override
    public void put(DataWord key, DataWord value) {
        storage.put(key, value);
        this.setDirty(true);
    }

    @Override
    public DataWord get(DataWord key) {

        DataWord dataWord = new DataWord();
        DataWord value = storage.get(key);
        if (value != null)
            value = value.clone();
        else{
            if (origContract == null) return null;
            value = origContract.get(key);
            storage.put(key.clone(), value == null ? dataWord.getZero().clone() : value.clone());
        }

        if (value == null || value.isZero())
            return null;
        else
            return value;
    }

    @Override
    public byte[] getStorageHash() { // todo: unsupported

        SecureTrie storageTrie = new SecureTrie((byte[]) null);

        for (DataWord key : storage.keySet()) {

            DataWord value = storage.get(key);

            storageTrie.put(key.getData(),
                    RLP.encodeElement(value.getNoLeadZeroesData()));
        }

        return storageTrie.getRootHash();
    }

    @Override
    public void decode(byte[] rlpCode) {
        throw new RuntimeException("Not supported by this implementation.");
    }

    @Override
    public byte[] getEncoded() {
        throw new RuntimeException("Not supported by this implementation.");
    }

    @Override
    public Map<DataWord, DataWord> getStorage() {
        return unmodifiableMap(storage);
    }

    @Override
    public Map<DataWord, DataWord> getStorage(Collection<DataWord> keys) {
        if (keys == null) return getStorage();

        Map<DataWord, DataWord> result = new HashMap<>();
        for (DataWord key : keys) {
            result.put(key, storage.get(key));
        }
        return unmodifiableMap(result);
    }

    @Override
    public int getStorageSize() {
        return (origContract == null)
                ? storage.size()
                : origContract.getStorageSize();
    }

    @Override
    public Set<DataWord> getStorageKeys() {
        return (origContract == null)
                ? storage.keySet()
                : origContract.getStorageKeys();
    }

    @Override
    public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {

        for (int i = 0; i < storageKeys.size(); ++i){

            DataWord key   = storageKeys.get(i);
            DataWord value = storageValues.get(i);

            if (value.isZero())
                storage.put(key, null);
        }

    }

    @Override
    public void setStorage(Map<DataWord, DataWord> storage) {
        this.storage = storage;
    }

    @Override
    public byte[] getAddress() {
         return (origContract == null) ? null : origContract.getAddress();
    }

    @Override
    public void setAddress(byte[] address) {
        if (origContract != null) origContract.setAddress(address);
    }

    @Override
    public ContractDetails clone() {

        ContractDetailsCacheImpl contractDetails = new ContractDetailsCacheImpl(origContract);

        Object storageClone = ((HashMap<DataWord, DataWord>)storage).clone();

        contractDetails.setCode(this.getCode());
        contractDetails.setStorage( (HashMap<DataWord, DataWord>) storageClone);
        return contractDetails;
    }

    @Override
    public void syncStorage() {
        if (origContract != null) origContract.syncStorage();
    }

    public void commit(){

        if (origContract == null) return;

        for (DataWord key : storage.keySet()) {
            origContract.put(key, storage.get(key));
        }

        if (origContract instanceof AbstractContractDetails) {
            ((AbstractContractDetails) origContract).appendCodes(getCodes());
        } else {
            origContract.setCode(getCode());
        }
        origContract.setDirty(this.isDirty() || origContract.isDirty());
    }


    @Override
    public ContractDetails getSnapshotTo(byte[] hash) {
        throw new UnsupportedOperationException("No snapshot option during cache state");
    }
}

