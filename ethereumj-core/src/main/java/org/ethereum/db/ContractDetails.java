/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.db;

import org.ethereum.vm.DataWord;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public interface ContractDetails {

    void put(DataWord key, DataWord value);

    DataWord get(DataWord key);

    byte[] getCode();

    void setCode(byte[] code);

    byte[] getCode(byte[] codeHash);

    byte[] getStorageHash();

    void decode(byte[] rlpCode);

    boolean isDirty();

    void setDirty(boolean dirty);

    boolean isDeleted();

    void setDeleted(boolean deleted);

    byte[] getEncoded();

    int getStorageSize();

    Set<DataWord> getStorageKeys();

    Map<DataWord, DataWord> getStorage(@Nullable Collection<DataWord> keys);

    Map<DataWord, DataWord> getStorage();

    void setStorage(Map<DataWord, DataWord> storage);

    void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues);

    byte[] getAddress();

    void setAddress(byte[] address);

    ContractDetails clone();

    String toString();

    void syncStorage();

    ContractDetails getSnapshotTo(byte[] hash);
}
