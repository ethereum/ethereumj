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

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Repository;
import org.ethereum.vm.DataWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Repository delegating all calls to the last Repository
 *
 * Created by Anton Nashatyrev on 22.12.2016.
 */
@Component
public class RepositoryWrapper implements Repository {

    @Autowired
    BlockchainImpl blockchain;

    public RepositoryWrapper() {
    }

    @Override
    public AccountState createAccount(byte[] addr) {
        return blockchain.getRepository().createAccount(addr);
    }

    @Override
    public boolean isExist(byte[] addr) {
        return blockchain.getRepository().isExist(addr);
    }

    @Override
    public AccountState getAccountState(byte[] addr) {
        return blockchain.getRepository().getAccountState(addr);
    }

    @Override
    public void delete(byte[] addr) {
        blockchain.getRepository().delete(addr);
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {
        return blockchain.getRepository().increaseNonce(addr);
    }

    @Override
    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        return blockchain.getRepository().setNonce(addr, nonce);
    }

    @Override
    public BigInteger getNonce(byte[] addr) {
        return blockchain.getRepository().getNonce(addr);
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return blockchain.getRepository().getContractDetails(addr);
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return blockchain.getRepository().hasContractDetails(addr);
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        blockchain.getRepository().saveCode(addr, code);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        return blockchain.getRepository().getCode(addr);
    }

    @Override
    public byte[] getCodeHash(byte[] addr) {
        return blockchain.getRepository().getCodeHash(addr);
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        blockchain.getRepository().addStorageRow(addr, key, value);
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return blockchain.getRepository().getStorageValue(addr, key);
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        return blockchain.getRepository().getBalance(addr);
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        return blockchain.getRepository().addBalance(addr, value);
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return blockchain.getRepository().getAccountsKeys();
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        blockchain.getRepository().dumpState(block, gasUsed, txNumber, txHash);
    }

    @Override
    public Repository startTracking() {
        return blockchain.getRepository().startTracking();
    }

    @Override
    public void flush() {
        blockchain.getRepository().flush();
    }

    @Override
    public void flushNoReconnect() {
        blockchain.getRepository().flushNoReconnect();
    }

    @Override
    public void commit() {
        blockchain.getRepository().commit();
    }

    @Override
    public void rollback() {
        blockchain.getRepository().rollback();
    }

    @Override
    public void syncToRoot(byte[] root) {
        blockchain.getRepository().syncToRoot(root);
    }

    @Override
    public boolean isClosed() {
        return blockchain.getRepository().isClosed();
    }

    @Override
    public void close() {
        blockchain.getRepository().close();
    }

    @Override
    public void reset() {
        blockchain.getRepository().reset();
    }

    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates, HashMap<ByteArrayWrapper, ContractDetails> contractDetailes) {
        blockchain.getRepository().updateBatch(accountStates, contractDetailes);
    }

    @Override
    public byte[] getRoot() {
        return blockchain.getRepository().getRoot();
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
        blockchain.getRepository().loadAccount(addr, cacheAccounts, cacheDetails);
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        return blockchain.getRepository().getSnapshotTo(root);
    }

    @Override
    public int getStorageSize(byte[] addr) {
        return blockchain.getRepository().getStorageSize(addr);
    }

    @Override
    public Set<DataWord> getStorageKeys(byte[] addr) {
        return blockchain.getRepository().getStorageKeys(addr);
    }

    @Override
    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
        return blockchain.getRepository().getStorage(addr, keys);
    }
}
