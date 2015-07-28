package org.ethereum.db;

import org.ethereum.trie.SecureTrie;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;
import org.spongycastle.util.encoders.Hex;

import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * @author Roman Mandeleil
 * @since 24.06.2014
 */
public class ContractDetailsCacheImpl implements ContractDetails {

    private Map<DataWord, DataWord> storage = new HashMap<>();

    ContractDetails origContract = new ContractDetailsImpl();

    private byte[] code = EMPTY_BYTE_ARRAY;

    private boolean dirty = false;
    private boolean deleted = false;


    public ContractDetailsCacheImpl(ContractDetails origContract) {
        this.origContract = origContract;
        this.code = origContract != null ? origContract.getCode() : EMPTY_BYTE_ARRAY;
    }

    @Override
    public void put(DataWord key, DataWord value) {
        storage.put(key, value);
        this.setDirty(true);
    }

    @Override
    public DataWord get(DataWord key) {

        DataWord value = storage.get(key);
        if (value != null)
            value = value.clone();
        else{
            if (origContract == null) return null;
            value = origContract.get(key);
            storage.put(key.clone(), value == null ? DataWord.ZERO.clone() : value.clone());
        }

        if (value == null || value.isZero())
            return null;
        else
            return value;
    }

    @Override
    public byte[] getCode() {
        return code;
    }

    @Override
    public void setCode(byte[] code) {
        this.code = code;
    }

    @Override
    public byte[] getStorageHash() { // todo: unsupported

        SecureTrie storageTrie = new SecureTrie(null);

        for (DataWord key : storage.keySet()) {

            DataWord value = storage.get(key);

            storageTrie.update(key.getData(),
                    RLP.encodeElement(value.getNoLeadZeroesData()));
        }

        return storageTrie.getRootHash();
    }

    @Override
    public void decode(byte[] rlpCode) {
        RLPList data = RLP.decode2(rlpCode);
        RLPList rlpList = (RLPList) data.get(0);

        RLPList keys = (RLPList) rlpList.get(0);
        RLPList values = (RLPList) rlpList.get(1);
        RLPElement code = rlpList.get(2);


        for (int i = 0; i < keys.size(); ++i){

            RLPItem key   = (RLPItem)keys.get(i);
            RLPItem value = (RLPItem)values.get(i);

            storage.put(new DataWord(key.getRLPData()), new DataWord(value.getRLPData()));
        }

        this.code = (code.getRLPData() == null) ? EMPTY_BYTE_ARRAY : code.getRLPData();
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }


    @Override
    public byte[] getEncoded() {

        byte[][] keys = new byte[storage.size()][];
        byte[][] values = new byte[storage.size()][];

        int i = 0;
        for (DataWord key : storage.keySet()){

            DataWord value = storage.get(key);

            keys[i] = RLP.encodeElement(key.getData());
            values[i] = RLP.encodeElement(value.getNoLeadZeroesData());

            ++i;
        }

        byte[] rlpKeysList = RLP.encodeList(keys);
        byte[] rlpValuesList = RLP.encodeList(values);
        byte[] rlpCode = RLP.encodeElement(code);

        return RLP.encodeList(rlpKeysList, rlpValuesList, rlpCode);
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
    public String toString() {

        String ret = "  Code: " + Hex.toHexString(code) + "\n";
        ret += "  Storage: " + getStorage().toString();

        return ret;
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

        origContract.setCode(code);
        origContract.setDirty(this.dirty || origContract.isDirty());
    }


    @Override
    public ContractDetails getSnapshotTo(byte[] hash) {
        throw new UnsupportedOperationException("No snapshot option during cache state");
    }
}

