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
package org.ethereum.vm.program;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.listener.ProgramListener;

public class NullStorage implements Storage {

    private final Repository repository;

    public NullStorage() {
        repository = new RepositoryImpl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ethereum.vm.program.Storage#setProgramListener(org.ethereum.vm.program.
     * listener.ProgramListener)
     */
    @Override
    public void setProgramListener(ProgramListener listener) {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#createAccount(byte[])
     */
    @Override
    public AccountState createAccount(byte[] addr) {
        return new AccountState(new BigInteger("0"), new BigInteger("0"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#isExist(byte[])
     */
    @Override
    public boolean isExist(byte[] addr) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getAccountState(byte[])
     */
    @Override
    public AccountState getAccountState(byte[] addr) {
        return new AccountState(new BigInteger("0"), new BigInteger("0"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#delete(byte[])
     */
    @Override
    public void delete(byte[] addr) {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#increaseNonce(byte[])
     */
    @Override
    public BigInteger increaseNonce(byte[] addr) {
        return new BigInteger("0");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#setNonce(byte[], java.math.BigInteger)
     */
    @Override
    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        return new BigInteger("0");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getNonce(byte[])
     */
    @Override
    public BigInteger getNonce(byte[] addr) {
        return new BigInteger("0");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getContractDetails(byte[])
     */
    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return new ContractDetails() {
            @Override
            public ContractDetails clone() {
                return this;
            }

            @Override
            public void syncStorage() {
                // Intentionally left blank
            }

            @Override
            public void setStorage(Map<DataWord, DataWord> storage) {
                // Intentionally left blank
            }

            @Override
            public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {
                // Intentionally left blank
            }

            @Override
            public void setDirty(boolean dirty) {
                // Intentionally left blank
            }

            @Override
            public void setDeleted(boolean deleted) {
                // Intentionally left blank
            }

            @Override
            public void setCode(byte[] code) {
                // Intentionally left blank
            }

            @Override
            public void setAddress(byte[] address) {
                // Intentionally left blank
            }

            @Override
            public void put(DataWord key, DataWord value) {
                // Intentionally left blank
            }

            @Override
            public boolean isDirty() {
                return false;
            }

            @Override
            public boolean isDeleted() {
                return false;
            }

            @Override
            public int getStorageSize() {
                return 0;
            }

            @Override
            public Set<DataWord> getStorageKeys() {
                return new HashSet<>();
            }

            @Override
            public byte[] getStorageHash() {
                return new byte[0];
            }

            @Override
            public Map<DataWord, DataWord> getStorage() {
                return new HashMap<>();
            }

            @Override
            public Map<DataWord, DataWord> getStorage(Collection<DataWord> keys) {
                return new HashMap<>();
            }

            @Override
            public ContractDetails getSnapshotTo(byte[] hash) {
                return this;
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }

            @Override
            public byte[] getCode(byte[] codeHash) {
                return new byte[0];
            }

            @Override
            public byte[] getCode() {
                return new byte[0];
            }

            @Override
            public byte[] getAddress() {
                return new byte[0];
            }

            @Override
            public DataWord get(DataWord key) {
                return key;
            }

            @Override
            public void decode(byte[] rlpCode) {
                // Intentionally left blank
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#hasContractDetails(byte[])
     */
    @Override
    public boolean hasContractDetails(byte[] addr) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#saveCode(byte[], byte[])
     */
    @Override
    public void saveCode(byte[] addr, byte[] code) {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getCode(byte[])
     */
    @Override
    public byte[] getCode(byte[] addr) {
        byte[] nullImp = { 0 };
        return nullImp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getCodeHash(byte[])
     */
    @Override
    public byte[] getCodeHash(byte[] addr) {
        byte[] nullImp = { 0 };
        return nullImp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#addStorageRow(byte[],
     * org.ethereum.vm.DataWord, org.ethereum.vm.DataWord)
     */
    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getStorageValue(byte[],
     * org.ethereum.vm.DataWord)
     */
    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return new DataWord();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getBalance(byte[])
     */
    @Override
    public BigInteger getBalance(byte[] addr) {
        return new BigInteger("0");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#addBalance(byte[], java.math.BigInteger)
     */
    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        return new BigInteger("0");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getAccountsKeys()
     */
    @Override
    public Set<byte[]> getAccountsKeys() {
        return new HashSet<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#dumpState(org.ethereum.core.Block, long,
     * int, byte[])
     */
    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#startTracking()
     */
    @Override
    public Repository startTracking() {
        return repository;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#flush()
     */
    @Override
    public void flush() {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#flushNoReconnect()
     */
    @Override
    public void flushNoReconnect() {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#commit()
     */
    @Override
    public void commit() {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#rollback()
     */
    @Override
    public void rollback() {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#syncToRoot(byte[])
     */
    @Override
    public void syncToRoot(byte[] root) {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#isClosed()
     */
    @Override
    public boolean isClosed() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#close()
     */
    @Override
    public void close() {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#reset()
     */
    @Override
    public void reset() {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#updateBatch(java.util.HashMap,
     * java.util.HashMap)
     */
    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates,
            HashMap<ByteArrayWrapper, ContractDetails> contractDetails) {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getRoot()
     */
    @Override
    public byte[] getRoot() {
        byte[] nullImp = { 0 };
        return nullImp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#loadAccount(byte[], java.util.HashMap,
     * java.util.HashMap)
     */
    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts,
            HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
        // Intentionally left blank
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getSnapshotTo(byte[])
     */
    @Override
    public Repository getSnapshotTo(byte[] root) {
        return repository;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getStorageSize(byte[])
     */
    @Override
    public int getStorageSize(byte[] addr) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getStorageKeys(byte[])
     */
    @Override
    public Set<DataWord> getStorageKeys(byte[] addr) {
        return new HashSet<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getStorage(byte[], java.util.Collection)
     */
    @Override
    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
        return new HashMap<>();
    }
}
