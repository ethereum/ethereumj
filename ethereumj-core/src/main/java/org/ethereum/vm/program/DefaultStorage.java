/*
 * Copyright (c) [2018] [ <ether.camp> ]
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
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.listener.ProgramListener;

public class DefaultStorage implements Storage {

    private final Repository repository;
    private final DataWord address;
    private ProgramListener programListener;

    public DefaultStorage(ProgramInvoke programInvoke) {
        this.address = programInvoke.getOwnerAddress();
        this.repository = programInvoke.getRepository();
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
        this.programListener = listener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#createAccount(byte[])
     */
    @Override
    public AccountState createAccount(byte[] addr) {
        return repository.createAccount(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#isExist(byte[])
     */
    @Override
    public boolean isExist(byte[] addr) {
        return repository.isExist(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getAccountState(byte[])
     */
    @Override
    public AccountState getAccountState(byte[] addr) {
        return repository.getAccountState(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#delete(byte[])
     */
    @Override
    public void delete(byte[] addr) {
        if (canListenTrace(addr))
            programListener.onStorageClear();
        repository.delete(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#increaseNonce(byte[])
     */
    @Override
    public BigInteger increaseNonce(byte[] addr) {
        return repository.increaseNonce(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#setNonce(byte[], java.math.BigInteger)
     */
    @Override
    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        return repository.setNonce(addr, nonce);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getNonce(byte[])
     */
    @Override
    public BigInteger getNonce(byte[] addr) {
        return repository.getNonce(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getContractDetails(byte[])
     */
    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return repository.getContractDetails(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#hasContractDetails(byte[])
     */
    @Override
    public boolean hasContractDetails(byte[] addr) {
        return repository.hasContractDetails(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#saveCode(byte[], byte[])
     */
    @Override
    public void saveCode(byte[] addr, byte[] code) {
        repository.saveCode(addr, code);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getCode(byte[])
     */
    @Override
    public byte[] getCode(byte[] addr) {
        return repository.getCode(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getCodeHash(byte[])
     */
    @Override
    public byte[] getCodeHash(byte[] addr) {
        return repository.getCodeHash(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#addStorageRow(byte[],
     * org.ethereum.vm.DataWord, org.ethereum.vm.DataWord)
     */
    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        if (canListenTrace(addr))
            programListener.onStoragePut(key, value);
        repository.addStorageRow(addr, key, value);
    }

    private boolean canListenTrace(byte[] address) {
        return (programListener != null) && this.address.equals(new DataWord(address));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getStorageValue(byte[],
     * org.ethereum.vm.DataWord)
     */
    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return repository.getStorageValue(addr, key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getBalance(byte[])
     */
    @Override
    public BigInteger getBalance(byte[] addr) {
        return repository.getBalance(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#addBalance(byte[], java.math.BigInteger)
     */
    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        return repository.addBalance(addr, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getAccountsKeys()
     */
    @Override
    public Set<byte[]> getAccountsKeys() {
        return repository.getAccountsKeys();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#dumpState(org.ethereum.core.Block, long,
     * int, byte[])
     */
    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        repository.dumpState(block, gasUsed, txNumber, txHash);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#startTracking()
     */
    @Override
    public Repository startTracking() {
        return repository.startTracking();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#flush()
     */
    @Override
    public void flush() {
        repository.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#flushNoReconnect()
     */
    @Override
    public void flushNoReconnect() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#commit()
     */
    @Override
    public void commit() {
        repository.commit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#rollback()
     */
    @Override
    public void rollback() {
        repository.rollback();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#syncToRoot(byte[])
     */
    @Override
    public void syncToRoot(byte[] root) {
        repository.syncToRoot(root);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#isClosed()
     */
    @Override
    public boolean isClosed() {
        return repository.isClosed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#close()
     */
    @Override
    public void close() {
        repository.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#reset()
     */
    @Override
    public void reset() {
        repository.reset();
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
        for (ByteArrayWrapper address : contractDetails.keySet()) {
            if (!canListenTrace(address.getData()))
                return;

            ContractDetails details = contractDetails.get(address);
            if (details.isDeleted()) {
                programListener.onStorageClear();
            } else if (details.isDirty()) {
                for (Map.Entry<DataWord, DataWord> entry : details.getStorage().entrySet()) {
                    programListener.onStoragePut(entry.getKey(), entry.getValue());
                }
            }
        }
        repository.updateBatch(accountStates, contractDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getRoot()
     */
    @Override
    public byte[] getRoot() {
        return repository.getRoot();
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
        repository.loadAccount(addr, cacheAccounts, cacheDetails);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getSnapshotTo(byte[])
     */
    @Override
    public Repository getSnapshotTo(byte[] root) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getStorageSize(byte[])
     */
    @Override
    public int getStorageSize(byte[] addr) {
        return repository.getStorageSize(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getStorageKeys(byte[])
     */
    @Override
    public Set<DataWord> getStorageKeys(byte[] addr) {
        return repository.getStorageKeys(addr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ethereum.vm.program.Storage#getStorage(byte[], java.util.Collection)
     */
    @Override
    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
        return repository.getStorage(addr, keys);
    }
}
