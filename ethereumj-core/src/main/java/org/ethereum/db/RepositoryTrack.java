package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.vm.DataWord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.ethereum.crypto.HashUtil.EMPTY_DATA_HASH;
import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.crypto.SHA3Helper.sha3;
import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * @author Roman Mandeleil
 * @since 17.11.2014
 */
@Component
@Scope("prototype")
public class RepositoryTrack implements Repository {

    private static final Logger logger = LoggerFactory.getLogger("repository");

    HashMap<ByteArrayWrapper, AccountState> cacheAccounts = new HashMap<>();
    HashMap<ByteArrayWrapper, ContractDetails> cacheDetails = new HashMap<>();

    Repository repository;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    SystemProperties config;

    // used by Spring wiring
    public RepositoryTrack(Repository repository) {
        this.repository = repository;
    }

    public RepositoryTrack(Repository repository, SystemProperties config) {
        this.repository = repository;
        this.config = config;
    }

    @Override
    public AccountState createAccount(byte[] addr) {

        synchronized (repository) {
            logger.trace("createAccount: [{}]", Hex.toHexString(addr));

            AccountState accountState = new AccountState(config.getBlockchainConfig().getCommonConstants().getInitialNonce(),
                    BigInteger.ZERO);
            cacheAccounts.put(wrap(addr), accountState);

            ContractDetails contractDetails = new ContractDetailsCacheImpl(null);
            contractDetails.setDirty(true);
            cacheDetails.put(wrap(addr), contractDetails);

            return accountState;
        }
    }

    @Override
    public AccountState getAccountState(byte[] addr) {

        synchronized (repository) {

            AccountState accountState = cacheAccounts.get(wrap(addr));

            if (accountState == null) {
                repository.loadAccount(addr, cacheAccounts, cacheDetails);

                accountState = cacheAccounts.get(wrap(addr));
            }
            return accountState;
        }
    }

    @Override
    public boolean isExist(byte[] addr) {

        synchronized (repository) {
            AccountState accountState = cacheAccounts.get(wrap(addr));
            if (accountState != null) return !accountState.isDeleted();

            return repository.isExist(addr);
        }
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {

        synchronized (repository) {
            ContractDetails contractDetails = cacheDetails.get(wrap(addr));

            if (contractDetails == null) {
                repository.loadAccount(addr, cacheAccounts, cacheDetails);
                contractDetails = cacheDetails.get(wrap(addr));
            }

            return contractDetails;
        }
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        synchronized (repository) {
            ContractDetails contractDetails = cacheDetails.get(wrap(addr));

            if (contractDetails == null) {
                return repository.hasContractDetails(addr);
            } else {
                return true;
            }
        }
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts,
                            HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {

        synchronized (repository) {
            AccountState accountState = this.cacheAccounts.get(wrap(addr));
            ContractDetails contractDetails = this.cacheDetails.get(wrap(addr));

            if (accountState == null) {
                repository.loadAccount(addr, this.cacheAccounts, this.cacheDetails);
                accountState = this.cacheAccounts.get(wrap(addr));
                contractDetails = this.cacheDetails.get(wrap(addr));
            }

            cacheAccounts.put(wrap(addr), accountState.clone());
            ContractDetails contractDetailsLvl2 = new ContractDetailsCacheImpl(contractDetails);
            cacheDetails.put(wrap(addr), contractDetailsLvl2);
        }
    }


    @Override
    public void delete(byte[] addr) {
        logger.trace("delete account: [{}]", Hex.toHexString(addr));

        synchronized (repository) {
            getAccountState(addr).setDeleted(true);
            getContractDetails(addr).setDeleted(true);
        }
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {

        synchronized (repository) {
            AccountState accountState = getAccountState(addr);

            if (accountState == null)
                accountState = createAccount(addr);

            getContractDetails(addr).setDirty(true);

            BigInteger saveNonce = accountState.getNonce();
            accountState.incrementNonce();

            logger.trace("increase nonce addr: [{}], from: [{}], to: [{}]", Hex.toHexString(addr),
                    saveNonce, accountState.getNonce());

            return accountState.getNonce();
        }
    }

    public BigInteger setNonce(byte[] addr, BigInteger bigInteger) {
        synchronized (repository) {
            AccountState accountState = getAccountState(addr);

            if (accountState == null)
                accountState = createAccount(addr);

            getContractDetails(addr).setDirty(true);

            BigInteger saveNonce = accountState.getNonce();
            accountState.setNonce(bigInteger);

            logger.trace("increase nonce addr: [{}], from: [{}], to: [{}]", Hex.toHexString(addr),
                    saveNonce, accountState.getNonce());

            return accountState.getNonce();
        }
    }


    @Override
    public BigInteger getNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? config.getBlockchainConfig().getCommonConstants().getInitialNonce() : accountState.getNonce();
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        if (!isExist(addr)) return BigInteger.ZERO;
        AccountState accountState = getAccountState(addr);
        return accountState == null ? AccountState.EMPTY_BALANCE : accountState.getBalance();
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {

        synchronized (repository) {
            AccountState accountState = getAccountState(addr);
            if (accountState == null) {
                accountState = createAccount(addr);
            }

            getContractDetails(addr).setDirty(true);
            BigInteger newBalance = accountState.addToBalance(value);

            logger.trace("adding to balance addr: [{}], balance: [{}], delta: [{}]", Hex.toHexString(addr),
                    newBalance, value);

            return newBalance;
        }
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        logger.trace("saving code addr: [{}], code: [{}]", Hex.toHexString(addr),
                Hex.toHexString(code));
        synchronized (repository) {
            getContractDetails(addr).setCode(code);
            getContractDetails(addr).setDirty(true);
            getAccountState(addr).setCodeHash(sha3(code));
        }
    }

    @Override
    public byte[] getCode(byte[] addr) {

        synchronized (repository) {
            if (!isExist(addr))
                return EMPTY_BYTE_ARRAY;

            byte[] codeHash = getAccountState(addr).getCodeHash();

            return getContractDetails(addr).getCode(codeHash);
        }
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {

        logger.trace("add storage row, addr: [{}], key: [{}] val: [{}]", Hex.toHexString(addr),
                key.toString(), value.toString());

        synchronized (repository) {
            getContractDetails(addr).put(key, value);
        }
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        synchronized (repository) {
            return getContractDetails(addr).get(key);
        }
    }


    @Override
    public Set<byte[]> getAccountsKeys() {
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
        logger.trace("start tracking: {}", this);

        Repository repository = applicationContext == null ? new RepositoryTrack(this, config) :
                applicationContext.getBean(RepositoryTrack.class, this);

        return repository;
    }


    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushNoReconnect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void commit() {

        synchronized (repository) {
            for (Map.Entry<ByteArrayWrapper, ContractDetails> entry : cacheDetails.entrySet()) {
                ContractDetailsCacheImpl contractDetailsCache = (ContractDetailsCacheImpl) entry.getValue();
                contractDetailsCache.commit();

                if (contractDetailsCache.origContract == null && repository.hasContractDetails(entry.getKey().getData())) {
                    // in forked block the contract account might not exist thus it is created without
                    // origin, but on the main chain details can contain data which should be merged
                    // into a single storage trie so both branches with different stateRoots are valid
                    contractDetailsCache.origContract = repository.getContractDetails(entry.getKey().getData());
                    contractDetailsCache.commit();
                }
            }

            repository.updateBatch(cacheAccounts, cacheDetails);
            cacheAccounts.clear();
            cacheDetails.clear();
            logger.trace("committed changes: {}", this);
        }
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

        synchronized (repository) {
            for (ByteArrayWrapper hash : accountStates.keySet()) {
                cacheAccounts.put(hash, accountStates.get(hash));
            }

            for (ByteArrayWrapper hash : contractDetailes.keySet()) {

                ContractDetailsCacheImpl contractDetailsCache = (ContractDetailsCacheImpl) contractDetailes.get(hash);
                if (contractDetailsCache.origContract != null && !(contractDetailsCache.origContract instanceof ContractDetailsImpl))
                    cacheDetails.put(hash, contractDetailsCache.origContract);
                else
                    cacheDetails.put(hash, contractDetailsCache);
            }
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

    @Override
    public Repository getSnapshotTo(byte[] root) {
        throw new UnsupportedOperationException();
    }

    public Repository getOriginRepository() {
        return (repository instanceof RepositoryTrack)
                ? ((RepositoryTrack) repository).getOriginRepository()
                : repository;
    }
}
