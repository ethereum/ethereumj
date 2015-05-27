package org.ethereum.db;

import org.ethereum.db.ContractDetailsImpl;
import org.ethereum.vm.DataWord;

import java.util.List;
import java.util.Map;

public interface ContractDetails {
    void put(DataWord key, DataWord value);

    DataWord get(DataWord key);

    byte[] getCode();

    void setCode(byte[] code);

    byte[] getStorageHash();

    void decode(byte[] rlpCode);

    void setDirty(boolean dirty);

    void setDeleted(boolean deleted);

    boolean isDirty();

    boolean isDeleted();

    byte[] getEncoded();

    Map<DataWord, DataWord> getStorage();

    void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues);

    void setStorage(Map<DataWord, DataWord> storage);

    ContractDetails clone();

    String toString();
}
