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
package org.ethereum.jsontestsuite.suite;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.util.ByteArrayMap;
import org.ethereum.util.ByteArraySet;
import org.ethereum.vm.DataWord;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Anton Nashatyrev on 01.12.2016.
 */
public class IterableTestRepository implements Repository {
    Repository src;

    Set<byte[]> accounts = new ByteArraySet();
    Map<byte[], Set<DataWord>> storageKeys = new ByteArrayMap<>();
    boolean environmental;

    private IterableTestRepository(Repository src, IterableTestRepository parent) {
        this.src = src;
        if (parent != null) {
            this.accounts = parent.accounts;
            this.storageKeys = parent.storageKeys;
            this.environmental = parent.environmental;
        }
    }

    public IterableTestRepository(Repository src) {
        this(src, null);
    }

    void addAccount(byte[] addr) {
        accounts.add(addr);
    }

    private void addStorageKey(byte[] acct, DataWord key) {
        addAccount(acct);
        Set<DataWord> keys = storageKeys.get(acct);
        if (keys == null) {
            keys = new HashSet<>();
            storageKeys.put(acct, keys);
        }
        keys.add(key);
    }

    @Override
    public Repository startTracking() {
        return new IterableTestRepository(src.startTracking(), this);
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        return new IterableTestRepository(src.getSnapshotTo(root), this);
    }

    @Override
    public AccountState createAccount(byte[] addr) {
        addAccount(addr);
        return src.createAccount(addr);
    }

    @Override
    public boolean isExist(byte[] addr) {
        return src.isExist(addr);
    }

    @Override
    public AccountState getAccountState(byte[] addr) {
        return src.getAccountState(addr);
    }

    @Override
    public void delete(byte[] addr) {
        addAccount(addr);
        src.delete(addr);
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {
        addAccount(addr);
        return src.increaseNonce(addr);
    }

    @Override
    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        return src.setNonce(addr, nonce);
    }

    @Override
    public BigInteger getNonce(byte[] addr) {
        return src.getNonce(addr);
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return new IterableContractDetails(src.getContractDetails(addr));
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return src.hasContractDetails(addr);
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        addAccount(addr);
        src.saveCode(addr, code);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        if (environmental) {
            if (!src.isExist(addr)) {
                createAccount(addr);
            }
        }
        return src.getCode(addr);
    }

    @Override
    public byte[] getCodeHash(byte[] addr) {
        return src.getCodeHash(addr);
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        addStorageKey(addr, key);
        src.addStorageRow(addr, key, value);
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return src.getStorageValue(addr, key);
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        if (environmental) {
            if (!src.isExist(addr)) {
                createAccount(addr);
            }
        }
        return src.getBalance(addr);
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        addAccount(addr);
        return src.addBalance(addr, value);
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        Set<byte[]> ret = new ByteArraySet();
        for (byte[] account : accounts) {
            if (isExist(account)) {
                ret.add(account);
            }
        }
        return ret;
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        src.dumpState(block, gasUsed, txNumber, txHash);
    }

    @Override
    public void flush() {
        src.flush();
    }

    @Override
    public void flushNoReconnect() {
        src.flushNoReconnect();
    }

    @Override
    public void commit() {
        src.commit();
    }

    @Override
    public void rollback() {
        src.rollback();
    }

    @Override
    public void syncToRoot(byte[] root) {
        src.syncToRoot(root);
    }

    @Override
    public boolean isClosed() {
        return src.isClosed();
    }

    @Override
    public void close() {
        src.close();
    }

    @Override
    public void reset() {
        src.reset();
    }

    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates, HashMap<ByteArrayWrapper, ContractDetails> contractDetailes) {
        src.updateBatch(accountStates, contractDetailes);
        for (ByteArrayWrapper wrapper : accountStates.keySet()) {
            addAccount(wrapper.getData());
        }

        for (Map.Entry<ByteArrayWrapper, ContractDetails> entry : contractDetailes.entrySet()) {
            for (DataWord key : entry.getValue().getStorageKeys()) {
                addStorageKey(entry.getKey().getData(), key);
            }
        }
    }

    @Override
    public byte[] getRoot() {
        return src.getRoot();
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
        src.loadAccount(addr, cacheAccounts, cacheDetails);
    }

    @Override
    public int getStorageSize(byte[] addr) {
        return src.getStorageSize(addr);
    }

    @Override
    public Set<DataWord> getStorageKeys(byte[] addr) {
        return src.getStorageKeys(addr);
    }

    @Override
    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
        return src.getStorage(addr, keys);
    }

    private class IterableContractDetails implements ContractDetails {
        ContractDetails src;

        public IterableContractDetails(ContractDetails src) {
            this.src = src;
        }

        @Override
        public void put(DataWord key, DataWord value) {
            addStorageKey(getAddress(), key);
            src.put(key, value);
        }

        @Override
        public DataWord get(DataWord key) {
            return src.get(key);
        }

        @Override
        public byte[] getCode() {
            return src.getCode();
        }

        @Override
        public byte[] getCode(byte[] codeHash) {
            return src.getCode(codeHash);
        }

        @Override
        public void setCode(byte[] code) {
            addAccount(getAddress());
            src.setCode(code);
        }

        @Override
        public byte[] getStorageHash() {
            return src.getStorageHash();
        }

        @Override
        public void decode(byte[] rlpCode) {
            src.decode(rlpCode);
        }

        @Override
        public void setDirty(boolean dirty) {
            src.setDirty(dirty);
        }

        @Override
        public void setDeleted(boolean deleted) {
            src.setDeleted(deleted);
        }

        @Override
        public boolean isDirty() {
            return src.isDirty();
        }

        @Override
        public boolean isDeleted() {
            return src.isDeleted();
        }

        @Override
        public byte[] getEncoded() {
            return src.getEncoded();
        }

        @Override
        public int getStorageSize() {
            Set<DataWord> set = storageKeys.get(getAddress());
            return set == null ? 0 : set.size();
        }

        @Override
        public Set<DataWord> getStorageKeys() {
            return getStorage().keySet();
        }

        @Override
        public Map<DataWord, DataWord> getStorage(@Nullable Collection<DataWord> keys) {
            throw new RuntimeException();
        }

        @Override
        public Map<DataWord, DataWord> getStorage() {
            Map<DataWord, DataWord> ret = new HashMap<>();
            Set<DataWord> set = storageKeys.get(getAddress());

            if (set == null) return Collections.emptyMap();

            for (DataWord key : set) {
                DataWord val = get(key);
                if (val != null && !val.isZero()) {
                    ret.put(key, get(key));
                }
            }
            return ret;
        }

        @Override
        public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {
            src.setStorage(storageKeys, storageValues);
        }

        @Override
        public void setStorage(Map<DataWord, DataWord> storage) {
            src.setStorage(storage);
        }

        @Override
        public byte[] getAddress() {
            return src.getAddress();
        }

        @Override
        public void setAddress(byte[] address) {
            src.setAddress(address);
        }

        @Override
        public ContractDetails clone() {
            return new IterableContractDetails(src.clone());
        }

        @Override
        public String toString() {
            return src.toString();
        }

        @Override
        public void syncStorage() {
            src.syncStorage();
        }

        @Override
        public ContractDetails getSnapshotTo(byte[] hash) {
            return new IterableContractDetails(src.getSnapshotTo(hash));
        }
    }
}
