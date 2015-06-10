package org.ethereum.db;

import org.ethereum.datasource.HashMapDB;
import org.ethereum.trie.SecureTrie;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;

import java.util.*;

import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * @author Roman Mandeleil
 * @since 24.06.2014
 */
public class ContractDetailsImpl implements ContractDetails {

    private byte[] rlpEncoded;

    private byte[] code = EMPTY_BYTE_ARRAY;

    private boolean dirty = false;
    private boolean deleted = false;

    private SecureTrie storageTrie = new SecureTrie(new HashMapDB());
    private Set<ByteArrayWrapper> keys = new HashSet<>(); // FIXME: sync to the disk

    public ContractDetailsImpl() {
    }

    public ContractDetailsImpl(byte[] rlpCode) {
        decode(rlpCode);
    }

    public ContractDetailsImpl(SecureTrie storageTrie, byte[] code) {
        this.storageTrie = storageTrie;
        this.code = code;
    }

    @Override
    public void put(DataWord key, DataWord value) {

        if (value.equals(DataWord.ZERO)){

            storageTrie.delete(key.getData());
            keys.remove(wrap(key.getData()));
        } else{

            storageTrie.update(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
            keys.add(wrap(key.getData()));
        }

        this.setDirty(true);
        this.rlpEncoded = null;
    }

    @Override
    public DataWord get(DataWord key) {

        byte[] data = storageTrie.get(key.getData());

        if (data.length == 0)
            return null;
        else{
            byte[] dataDecoded = RLP.decode2(data).get(0).getRLPData();
            return new DataWord(dataDecoded);
        }
    }

    @Override
    public byte[] getCode() {
        return code;
    }

    @Override
    public void setCode(byte[] code) {
        this.code = code;
        this.setDirty(true);
        this.rlpEncoded = null;
    }

    @Override
    public byte[] getStorageHash() {
        return storageTrie.getRootHash();
    }

    @Override
    public void decode(byte[] rlpCode) {
        RLPList data = RLP.decode2(rlpCode);
        RLPList rlpList = (RLPList) data.get(0);

        RLPItem storage = (RLPItem) rlpList.get(0);
        RLPElement code = rlpList.get(1);
        RLPList keys = (RLPList) rlpList.get(2);

        this.storageTrie.deserialize(storage.getRLPData());
        this.code = (code.getRLPData() == null) ? EMPTY_BYTE_ARRAY : code.getRLPData();

        for (int i = 0; i < keys.size(); ++i){
            byte[] key = keys.get(i).getRLPData();
            this.keys.add(wrap(key));
        }

        this.rlpEncoded = rlpCode;
    }

    @Override
    public byte[] getEncoded() {

        if (rlpEncoded == null) {

            byte[] storage = RLP.encodeElement(storageTrie.serialize());
            byte[] rlpCode = RLP.encodeElement(code);
            byte[] rlpKeys = RLP.encodeSet(keys);

            this.rlpEncoded = RLP.encodeList(storage, rlpCode, rlpKeys);
        }
        return rlpEncoded;
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
    public Map<DataWord, DataWord> getStorage() {

        Map<DataWord, DataWord> storage = new HashMap<>();

        for (ByteArrayWrapper keyBytes : keys){

            DataWord key = new DataWord(keyBytes);
            DataWord value = get(key);
            storage.put(key, value);
        }

        return storage;
    }

    @Override
    public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {

        for (int i = 0; i < storageKeys.size(); ++i)
            put(storageKeys.get(i), storageValues.get(i));
    }

    @Override
    public void setStorage(Map<DataWord, DataWord> storage) {

        for (DataWord key : storage.keySet()) {

            DataWord value = storage.get(key);
            put(key, value);
        }
    }


    @Override
    public ContractDetails clone() {

        // FIXME: clone is not working now !!!
        // FIXME: should be fixed

        byte[] cloneCode = Arrays.clone(this.getCode());

        storageTrie.getRoot();

        return new ContractDetailsImpl(null, cloneCode);
    }

    @Override
    public String toString() {

        String ret = "  Code: " + Hex.toHexString(code) + "\n";
        ret += "  Storage: " + getStorage().toString();

        return ret;
    }

}

