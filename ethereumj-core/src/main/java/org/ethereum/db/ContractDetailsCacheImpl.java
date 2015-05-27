package org.ethereum.db;

import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.ethereum.util.*;
import org.ethereum.vm.DataWord;
import org.spongycastle.util.encoders.Hex;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * @author Roman Mandeleil
 * @since 24.06.2014
 */
public class ContractDetailsCacheImpl implements ContractDetails {

    private Map<DataWord, DataWord> storage = new HashMap<>();


    private byte[] code = ByteUtil.EMPTY_BYTE_ARRAY;

    private boolean dirty = false;
    private boolean deleted = false;


    public ContractDetailsCacheImpl() {
    }

    public ContractDetailsCacheImpl(byte[] rlpCode) {
        decode(rlpCode);
    }

    public ContractDetailsCacheImpl(Map<DataWord, DataWord> storage, byte[] code) {
    }

    @Override
    public void put(DataWord key, DataWord value) {

        if (value.equals(DataWord.ZERO)) {
            storage.remove(key);
        } else {

            storage.put(key, value);
        }

        this.setDirty(true);
    }

    @Override
    public DataWord get(DataWord key) {

        DataWord value = storage.get(key);
        if (value != null) value = value.clone();
        return value;
    }

    @Override
    public byte[] getCode() {
        return code;
    }

    @Override
    public void setCode(byte[] code) {
        this.code = code;
        this.setDirty(true);
    }

    @Override
    public byte[] getStorageHash() {

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

        this.code = (code.getRLPData() == null) ? ByteUtil.EMPTY_BYTE_ARRAY : code.getRLPData();
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
        return Collections.unmodifiableMap(storage);
    }

    @Override
    public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {

        for (int i = 0; i < storageKeys.size(); ++i){

            DataWord key   = storageKeys.get(i);
            DataWord value = storageKeys.get(i);

            storage.put(key, value);
        }

    }

    @Override
    public void setStorage(Map<DataWord, DataWord> storage) {
        this.storage = storage;
    }


    @Override
    public ContractDetails clone() {

        ContractDetailsCacheImpl contractDetails = new ContractDetailsCacheImpl();

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

}

