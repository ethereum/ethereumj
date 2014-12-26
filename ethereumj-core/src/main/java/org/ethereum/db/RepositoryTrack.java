package org.ethereum.db;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.facade.Repository;
import org.ethereum.vm.DataWord;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;

import static org.ethereum.crypto.SHA3Helper.sha3;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * www.etherj.com
 *
 * @author Roman Mandeleil
 * @since 17.11.2014
 */

public class RepositoryTrack implements Repository {

    private static final Logger logger = LoggerFactory.getLogger("repository");

    HashMap<ByteArrayWrapper, AccountState> cacheAccounts = new HashMap<>();
    HashMap<ByteArrayWrapper, ContractDetails> cacheDetails = new HashMap<>();

    Repository repository;

    public RepositoryTrack() {
        this.repository = new RepositoryDummy();
    }

    public RepositoryTrack(Repository repository) {
        this.repository = repository;
    }

    @Override
    public AccountState createAccount(byte[] addr) {

        logger.trace("createAccount: [{}]", Hex.toHexString(addr));

        AccountState accountState = new AccountState();
        cacheAccounts.put(wrap(addr), accountState);

        ContractDetails contractDetails = new ContractDetails();
        cacheDetails.put(wrap(addr), contractDetails);

        return accountState;
    }

    @Override
    public AccountState getAccountState(byte[] addr) {

        AccountState accountState = cacheAccounts.get(wrap(addr));

        if (accountState == null) {
            repository.loadAccount(addr, cacheAccounts, cacheDetails);
            accountState = cacheAccounts.get(wrap(addr));
        }
        return accountState;
    }

    @Override
    public boolean isExist(byte[] addr) {

        AccountState accountState = cacheAccounts.get(wrap(addr));
        if (accountState != null) return !accountState.isDeleted();

        accountState = repository.getAccountState(addr);
        return accountState != null && !accountState.isDeleted();

    }


    @Override
    public ContractDetails getContractDetails(byte[] addr) {

        ContractDetails contractDetails = cacheDetails.get(wrap(addr));

        if (contractDetails == null) {
            repository.loadAccount(addr, cacheAccounts, cacheDetails);
            contractDetails = cacheDetails.get(wrap(addr));
        }

        return contractDetails;
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts,
                            HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {

        AccountState accountState = this.cacheAccounts.get(wrap(addr));
        ContractDetails contractDetails = this.cacheDetails.get(wrap(addr));

        if (accountState == null) {
            repository.loadAccount(addr, cacheAccounts, cacheDetails);
        } else {
            cacheAccounts.put(wrap(addr), accountState.clone());
            cacheDetails.put(wrap(addr), contractDetails.clone());
        }
    }


    @Override
    public void delete(byte[] addr) {

        logger.trace("delete account: [{}]", Hex.toHexString(addr));
        getAccountState(addr).setDeleted(true);
        getContractDetails(addr).setDeleted(true);
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {

        AccountState accountState = getAccountState(addr);

        if (accountState == null)
            accountState = createAccount(addr);

        BigInteger saveNonce = accountState.getNonce();
        accountState.incrementNonce();

        logger.trace("increase nonce addr: [{}], from: [{}], to: [{}]", Hex.toHexString(addr),
                saveNonce, accountState.getNonce());

        return accountState.getNonce();
    }

    public BigInteger setNonce(byte[] addr, BigInteger bigInteger) {
        AccountState accountState = getAccountState(addr);

        if (accountState == null)
            accountState = createAccount(addr);

        BigInteger saveNonce = accountState.getNonce();
        accountState.setNonce(bigInteger);

        logger.trace("increase nonce addr: [{}], from: [{}], to: [{}]", Hex.toHexString(addr),
                saveNonce, accountState.getNonce());

        return accountState.getNonce();

    }


    @Override
    public BigInteger getNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? BigInteger.ZERO : accountState.getNonce();
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? BigInteger.ZERO : accountState.getBalance();
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {

        AccountState accountState = getAccountState(addr);
        if (accountState == null) {
            accountState = createAccount(addr);
        }

        BigInteger newBalance = accountState.addToBalance(value);

        logger.trace("adding to balance addr: [{}], balance: [{}], delta: [{}]", Hex.toHexString(addr),
                newBalance, value);

        return newBalance;
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        logger.trace("saving code addr: [{}], code: [{}]", Hex.toHexString(addr),
                Hex.toHexString(code));
        getContractDetails(addr).setCode(code);
        getAccountState(addr).setCodeHash(sha3(code));
    }

    @Override
    public byte[] getCode(byte[] addr) {
        return getContractDetails(addr).getCode();
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {

        logger.trace("add storage row, addr: [{}], key: [{}] val: [{}]", Hex.toHexString(addr),
                key.toString(), value.toString());

        getContractDetails(addr).put(key, value);
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return getContractDetails(addr).get(key);
    }


    @Override
    public DBIterator getAccountsIterator() {
        throw new UnsupportedOperationException();
    }

    public Set<ByteArrayWrapper> getFullAddressSet() {
        return cacheAccounts.keySet();
    }


    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Repository startTracking() {
        logger.debug("start tracking");
        return new RepositoryTrack(this);
    }


    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void commit() {
        logger.debug("commit changes");
        repository.updateBatch(cacheAccounts, cacheDetails);
        cacheAccounts.clear();
        cacheDetails.clear();
    }


    @Override
    public void syncToRoot(byte[] root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback() {
        logger.debug("rollback changes");

        cacheAccounts.clear();
        cacheDetails.clear();
    }

    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates,
                            HashMap<ByteArrayWrapper, ContractDetails> contractDetailes) {

        for (ByteArrayWrapper hash : accountStates.keySet()) {
            cacheAccounts.put(hash, accountStates.get(hash));
        }

        for (ByteArrayWrapper hash : contractDetailes.keySet()) {
            cacheDetails.put(hash, contractDetailes.get(hash));
        }
    }

    @Override // that's the idea track is here not for root calculations
    public byte[] getRoot() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

}
