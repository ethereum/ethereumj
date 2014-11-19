package org.ethereum.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.*;
import org.ethereum.vm.DataWord;

/**
 * @author: Roman Mandeleil
 * Created on: 24/06/2014 00:12
 */
public class ContractDetails {

    private byte[] rlpEncoded;

    private List<DataWord> storageKeys   = new ArrayList<>();
    private List<DataWord> storageValues = new ArrayList<>();

    private byte[] code = ByteUtil.EMPTY_BYTE_ARRAY;

    private boolean dirty   = true;
    private boolean deleted = false;

    private Trie storageTrie = new TrieImpl(null);

    public ContractDetails() {
    }
	
    public ContractDetails(byte[] rlpCode) {
        decode(rlpCode);
    }

	public ContractDetails(Map<DataWord, DataWord> storage, byte[] code) {
	}

	public void put(DataWord key, DataWord value) {

        if (value.equals(DataWord.ZERO)) {

            storageTrie.delete(key.getData());
            int index = storageKeys.indexOf(key);
            if (index != -1) {
                storageKeys.remove(index);
                storageValues.remove(index);
            }
        } else {

            storageTrie.update(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
            int index = storageKeys.indexOf(key);
            if (index != -1) {
                storageKeys.remove(index);
                storageValues.remove(index);
            }
            storageKeys.add(key);
            storageValues.add(value);
        }

        this.setDirty(true);
        this.rlpEncoded = null;
	}

	public DataWord get(DataWord key) {

		if (storageKeys.size() == 0)
			return null;

		int foundIndex = storageKeys.indexOf(key);
		if (foundIndex != -1)
			return storageValues.get(foundIndex);
		else
			return null;
	}

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
        this.setDirty(true);
        this.rlpEncoded = null;
    }

    public byte[] getStorageHash() {

    	storageTrie = new TrieImpl(null);
        // calc the trie for root hash
        for (int i = 0; i < storageKeys.size(); ++i){
			storageTrie.update(storageKeys.get(i).getData(), RLP
					.encodeElement(storageValues.get(i).getNoLeadZeroesData()));
        }
        return storageTrie.getRootHash();
    }

	public void decode(byte[] rlpCode) {
		RLPList data = RLP.decode2(rlpCode);
		RLPList rlpList = (RLPList) data.get(0);

		RLPList keys = (RLPList) rlpList.get(0);
		RLPList values = (RLPList) rlpList.get(1);
		RLPElement code = (RLPElement) rlpList.get(2);

		if (keys.size() > 0) {
			storageKeys = new ArrayList<>();
			storageValues = new ArrayList<>();
		}

		for (int i = 0; i < keys.size(); ++i) {
			RLPItem rlpItem = (RLPItem) keys.get(i);
			storageKeys.add(new DataWord(rlpItem.getRLPData()));
		}

		for (int i = 0; i < values.size(); ++i) {
			RLPItem rlpItem = (RLPItem) values.get(i);
			storageValues.add(new DataWord(rlpItem.getRLPData()));
		}

		for (int i = 0; i < keys.size(); ++i) {
			DataWord key = storageKeys.get(i);
			DataWord value = storageValues.get(i);
            storageTrie.update(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
		}

		this.code = (code.getRLPData() == null) ? ByteUtil.EMPTY_BYTE_ARRAY : code.getRLPData();
		this.rlpEncoded = rlpCode;
	}

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isDeleted() {
        return deleted;
    }


	public byte[] getEncoded() {

		if (rlpEncoded == null) {

			int size = storageKeys == null ? 0 : storageKeys.size();

			byte[][] keys = new byte[size][];
			byte[][] values = new byte[size][];

			for (int i = 0; i < size; ++i) {
				DataWord key = storageKeys.get(i);
				keys[i] = RLP.encodeElement(key.getData());
			}
			for (int i = 0; i < size; ++i) {
				DataWord value = storageValues.get(i);
				values[i] = RLP.encodeElement(value.getNoLeadZeroesData());
			}

			byte[] rlpKeysList = RLP.encodeList(keys);
			byte[] rlpValuesList = RLP.encodeList(values);
			byte[] rlpCode = RLP.encodeElement(code);

			this.rlpEncoded = RLP.encodeList(rlpKeysList, rlpValuesList, rlpCode);
		}
		return rlpEncoded;
	}

    public Map<DataWord, DataWord> getStorage() {
        Map<DataWord, DataWord> storage = new HashMap<>();
		for (int i = 0; storageKeys != null && i < storageKeys.size(); ++i) {
			storage.put(storageKeys.get(i), storageValues.get(i));
		}
        return Collections.unmodifiableMap(storage);
    }

    public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {
        this.storageKeys = storageKeys;
        this.storageValues = storageValues;
    }


    public ContractDetails clone(){

        ContractDetails contractDetails = new ContractDetails();

        contractDetails.setCode(this.getCode());
        contractDetails.setStorage(new ArrayList<DataWord>(this.storageKeys)  ,
                                   new ArrayList<DataWord>(this.storageValues));
        return contractDetails;
    }

}

