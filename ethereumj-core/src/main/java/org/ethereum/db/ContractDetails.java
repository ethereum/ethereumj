package org.ethereum.db;

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

    Map<DataWord, DataWord> getStorage(int offset, int limit);

    int getStorageSize();

    void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues);

    void setStorage(Map<DataWord, DataWord> storage);

    byte[] getAddress();

    void setAddress(byte[] address);

    ContractDetails clone();

    String toString();

    void syncStorage();

    ContractDetails getSnapshotTo(byte[] hash);
}
