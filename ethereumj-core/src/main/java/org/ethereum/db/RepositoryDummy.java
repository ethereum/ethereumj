package org.ethereum.db;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.vm.DataWord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.ethereum.crypto.SHA3Helper.sha3;
import static org.ethereum.util.ByteUtil.wrap;

/**
 * @author Roman Mandeleil
 * @since 17.11.2014
 */
public class RepositoryDummy extends RepositoryImpl {

    private static final Logger logger = LoggerFactory.getLogger("repository");
    private Map<ByteArrayWrapper, AccountState> worldState = new HashMap<>();
    private Map<ByteArrayWrapper, ContractDetails> detailsDB = new HashMap<>();

    public RepositoryDummy() {
        super(false);
    }

    @Override
    public void reset() {

        worldState.clear();
        detailsDB.clear();
    }

    @Override
    public void close() {
        worldState.clear();
        detailsDB.clear();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> stateCache, HashMap<ByteArrayWrapper,
            ContractDetails> detailsCache) {

        for (ByteArrayWrapper hash : stateCache.keySet()) {

            AccountState accountState = stateCache.get(hash);
            ContractDetails contractDetails = detailsCache.get(hash);

            if (accountState.isDeleted()) {
                worldState.remove(hash);
                detailsDB.remove(hash);

                logger.debug("delete: [{}]",
                        Hex.toHexString(hash.getData()));

            } else {

                if (accountState.isDirty() || contractDetails.isDirty()) {
                    detailsDB.put(hash, contractDetails);
                    accountState.setStateRoot(contractDetails.getStorageHash());
                    accountState.setCodeHash(sha3(contractDetails.getCode()));
                    worldState.put(hash, accountState);
                    if (logger.isDebugEnabled()) {
                        logger.debug("update: [{}],nonce: [{}] balance: [{}] \n [{}]",
                                Hex.toHexString(hash.getData()),
                                accountState.getNonce(),
                                accountState.getBalance(),
                                contractDetails.getStorage());
                    }

                }

            }
        }

        stateCache.clear();
        detailsCache.clear();

    }


    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void syncToRoot(byte[] root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Repository startTracking() {
        return new RepositoryTrack(this);
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {

    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return null;
    }

    public Set<ByteArrayWrapper> getFullAddressSet() {
        return worldState.keySet();
    }


    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        AccountState account = getAccountState(addr);

        if (account == null)
            account = createAccount(addr);

        BigInteger result = account.addToBalance(value);
        worldState.put(wrap(addr), account);

        return result;
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        AccountState account = getAccountState(addr);

        if (account == null)
            return BigInteger.ZERO;

        return account.getBalance();
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        ContractDetails details = getContractDetails(addr);

        if (details == null)
            return null;

        return details.get(key);
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        ContractDetails details = getContractDetails(addr);

        if (details == null) {
            createAccount(addr);
            details = getContractDetails(addr);
        }

        details.put(key, value);
        detailsDB.put(wrap(addr), details);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        ContractDetails details = getContractDetails(addr);

        if (details == null)
            return null;

        return details.getCode();
    }


    @Override
    public void saveCode(byte[] addr, byte[] code) {
        ContractDetails details = getContractDetails(addr);

        if (details == null) {
            createAccount(addr);
            details = getContractDetails(addr);
        }

        details.setCode(code);
        detailsDB.put(wrap(addr), details);
    }

    @Override
    public BigInteger getNonce(byte[] addr) {
        AccountState account = getAccountState(addr);

        if (account == null)
            account = createAccount(addr);

        return account.getNonce();
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {
        AccountState account = getAccountState(addr);

        if (account == null)
            account = createAccount(addr);

        account.incrementNonce();
        worldState.put(wrap(addr), account);

        return account.getNonce();
    }

    public BigInteger setNonce(byte[] addr, BigInteger nonce) {

        AccountState account = getAccountState(addr);

        if (account == null)
            account = createAccount(addr);

        account.setNonce(nonce);
        worldState.put(wrap(addr), account);

        return account.getNonce();
    }


    @Override
    public void delete(byte[] addr) {
        worldState.remove(wrap(addr));
        detailsDB.remove(wrap(addr));
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {

        return detailsDB.get(wrap(addr));
    }


    @Override
    public AccountState getAccountState(byte[] addr) {
        return worldState.get(wrap(addr));
    }

    @Override
    public AccountState createAccount(byte[] addr) {
        AccountState accountState = new AccountState();
        worldState.put(wrap(addr), accountState);

        ContractDetails contractDetails = new ContractDetailsImpl();
        detailsDB.put(wrap(addr), contractDetails);

        return accountState;
    }


    @Override
    public boolean isExist(byte[] addr) {
        return getAccountState(addr) != null;
    }

    @Override
    public byte[] getRoot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {

        AccountState account = getAccountState(addr);
        ContractDetails details = getContractDetails(addr);

        if (account == null)
            account = new AccountState();
        else
            account = account.clone();

        if (details == null)
            details = new ContractDetailsImpl();
        else
            details = details.clone();

        cacheAccounts.put(wrap(addr), account);
        cacheDetails.put(wrap(addr), details);
    }
}
