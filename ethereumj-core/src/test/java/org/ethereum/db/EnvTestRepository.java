package org.ethereum.db;

import org.ethereum.core.*;
import org.ethereum.core.AccountState;
import org.ethereum.vm.DataWord;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;

/**
 * Repository for running GitHubVMTest.
 * The slightly modified behavior from original impl:
 * it creates empty account whenever it 'touched': getCode() or getBalance()
 * Created by Anton Nashatyrev on 03.11.2016.
 */
public class EnvTestRepository extends RepositoryImpl {

    RepositoryImpl src;

    public EnvTestRepository(RepositoryImpl src) {
        this.src = src;
    }

    @Override
    public AccountState createAccount(byte[] addr) {
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
        src.delete(addr);
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {
        return src.increaseNonce(addr);
    }

    @Override
    public BigInteger getNonce(byte[] addr) {
        return src.getNonce(addr);
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return src.getContractDetails(addr);
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return src.hasContractDetails(addr);
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        src.saveCode(addr, code);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        src.getOrCreateAccountState(addr);
        return src.getCode(addr);
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        src.addStorageRow(addr, key, value);
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return src.getStorageValue(addr, key);
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        src.getOrCreateAccountState(addr);
        return src.getBalance(addr);
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        return src.addBalance(addr, value);
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return src.getAccountsKeys();
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        src.dumpState(block, gasUsed, txNumber, txHash);
    }

    @Override
    public RepositoryImpl startTracking() {
        return new EnvTestRepository(src.startTracking());
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
    public Repository getSnapshotTo(byte[] root) {
        return src.getSnapshotTo(root);
    }

    @Override
    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        return src.setNonce(addr, nonce);
    }
}
