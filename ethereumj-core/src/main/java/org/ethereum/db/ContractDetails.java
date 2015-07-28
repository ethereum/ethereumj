package org.ethereum.db;

import org.ethereum.vm.DataWord;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    int getStorageSize();

    Set<DataWord> getStorageKeys();

    Map<DataWord,DataWord> getStorage(@Nullable Collection<DataWord> keys);

    Map<DataWord, DataWord> getStorage();

    void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues);

    void setStorage(Map<DataWord, DataWord> storage);

    byte[] getAddress();

    void setAddress(byte[] address);

    ContractDetails clone();

    String toString();

    void syncStorage();

    ContractDetails getSnapshotTo(byte[] hash);
}
