package org.ethereum.datasource.test;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Serializer;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.Value;
import org.ethereum.vm.DataWord;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public class RepositoryImpl implements Repository, org.ethereum.facade.Repository {

    protected RepositoryImpl parent;

    protected Source<byte[], AccountState> accountStateCache;
    protected Source<byte[], byte[]> codeCache;
    protected MultiCache<? extends CachedSource<DataWord, DataWord, byte[], byte[]>> storageCache;

    @Autowired
    protected SystemProperties config = SystemProperties.getDefault();

    protected RepositoryImpl() {
    }

    public RepositoryImpl(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,
                          MultiCache<? extends CachedSource<DataWord, DataWord, byte[], byte[]>> storageCache) {
        init(accountStateCache, codeCache, storageCache);
    }

    protected void init(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,
                        MultiCache<? extends CachedSource<DataWord, DataWord, byte[], byte[]>> storageCache) {
        this.accountStateCache = accountStateCache;
        this.codeCache = codeCache;
        this.storageCache = storageCache;
    }

    @Override
    public AccountState createAccount(byte[] addr) {
        AccountState state = new AccountState(config.getBlockchainConfig().getCommonConstants().getInitialNonce(),
                BigInteger.ZERO);
        accountStateCache.put(addr, state);
        return state;
    }

    @Override
    public boolean isExist(byte[] addr) {
        return getAccountState(addr) != null;
    }

    @Override
    public AccountState getAccountState(byte[] addr) {
        return accountStateCache.get(addr);
    }

    private AccountState getOrCreateAccountState(byte[] addr) {
        AccountState ret = accountStateCache.get(addr);
        if (ret == null) {
            ret = createAccount(addr);
        }
        return ret;
    }

    @Override
    public void delete(byte[] addr) {
        accountStateCache.delete(addr);
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {
        AccountState accountState = getOrCreateAccountState(addr).clone();
        accountState.incrementNonce();
        accountStateCache.put(addr, accountState);
        return accountState.getNonce();
    }

    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        AccountState accountState = getOrCreateAccountState(addr).clone();
        accountState.setNonce(nonce);
        accountStateCache.put(addr, accountState);
        return accountState.getNonce();
    }

    @Override
    public BigInteger getNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? config.getBlockchainConfig().getCommonConstants().getInitialNonce() :
                accountState.getNonce();
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return new ContractDetailsImpl(addr);
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return getContractDetails(addr) != null;
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        byte[] codeHash = HashUtil.sha3(code);
        codeCache.put(codeHash, code);
        AccountState accountState = getOrCreateAccountState(addr).clone();
        accountState.setCodeHash(codeHash);
        accountStateCache.put(addr, accountState);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        if (accountState != null) {
            byte[] codeHash = accountState.getCodeHash();
            return FastByteComparisons.equal(codeHash, HashUtil.EMPTY_DATA_HASH) ?
                    ByteUtil.EMPTY_BYTE_ARRAY : codeCache.get(codeHash);
        } else {
            return ByteUtil.EMPTY_BYTE_ARRAY;
        }
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        getOrCreateAccountState(addr);

        Source<DataWord, DataWord> contractStorage = storageCache.get(addr);
        contractStorage.put(key, value.isZero() ? null : value);
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? null : storageCache.get(addr).get(key);
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? BigInteger.ZERO : accountState.getBalance();
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        AccountState accountState = getOrCreateAccountState(addr).clone();
        accountState.addToBalance(value);
        accountStateCache.put(addr, accountState);
        return accountState.getBalance();
    }

    @Override
    public RepositoryImpl startTracking() {
        CachedSourceImpl.SimpleBytesKey<AccountState> trackAccountStateCache = new CachedSourceImpl.SimpleBytesKey<>(accountStateCache);
        CachedSourceImpl.SimpleBytesKey<byte[]> trackCodeCache = new CachedSourceImpl.SimpleBytesKey<>(codeCache);
        MultiCache<CachedSource<DataWord, DataWord, byte[], byte[]>> trackStorageCache = new MultiCache(storageCache) {
            @Override
            protected CachedSource create(byte[] key, CachedSource srcCache) {
                return new Simple<>(srcCache);
            }
        };

        RepositoryImpl ret = new RepositoryImpl(trackAccountStateCache, trackCodeCache, trackStorageCache);
        ret.parent = this;
        return ret;
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        return parent.getSnapshotTo(root);
    }

    @Override
    public void commit() {
        storageCache.flush();
        codeCache.flush();
        accountStateCache.flush();
    }

    @Override
    public void rollback() {
        // nothing to do, will be GCed
    }

    @Override
    public byte[] getRoot() {
        throw new RuntimeException("Not supported");
    }

    public String getTrieDump() {
        return dumpStateTrie();
    }

    public String dumpStateTrie() {
        throw new RuntimeException("Not supported");
    }

    public Value getState(byte[] stateRoot) {
        throw new RuntimeException("Not supported");
    }

    static class AccountStateSerializer implements Serializer<AccountState, byte[]> {
        @Override
        public byte[] serialize(AccountState object) {
            return object.getEncoded();
        }

        @Override
        public AccountState deserialize(byte[] stream) {
            return stream == null || stream.length == 0 ? null : new AccountState(stream);
        }
    }

    static class WordSerializer implements Serializer<DataWord, byte[]> {
        @Override
        public byte[] serialize(DataWord object) {
            return object.getData();
        }

        @Override
        public DataWord deserialize(byte[] stream) {
            return new DataWord(stream);
        }
    }

    static class TrieWordSerializer implements Serializer<DataWord, byte[]> {
        @Override
        public byte[] serialize(DataWord object) {
            return RLP.encodeElement(object.getNoLeadZeroesData());
        }

        @Override
        public DataWord deserialize(byte[] stream) {
            if (stream == null || stream.length == 0) return null;
            byte[] dataDecoded = RLP.decode2(stream).get(0).getRLPData();
            return new DataWord(dataDecoded);
        }
    }

    static class TrieCacheSerializer implements Serializer<Value, byte[]> {
        @Override
        public byte[] serialize(Value object) {
            return object.encode();
        }

        @Override
        public Value deserialize(byte[] stream) {
            return Value.fromRlpEncoded(stream);
        }
    }

    class ContractDetailsImpl implements ContractDetails {
        private byte[] address;

        public ContractDetailsImpl(byte[] address) {
            this.address = address;
        }

        @Override
        public void put(DataWord key, DataWord value) {
            RepositoryImpl.this.addStorageRow(address, key, value);
        }

        @Override
        public DataWord get(DataWord key) {
            return RepositoryImpl.this.getStorageValue(address, key);
        }

        @Override
        public byte[] getCode() {
            return RepositoryImpl.this.getCode(address);
        }

        @Override
        public byte[] getCode(byte[] codeHash) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setCode(byte[] code) {
            RepositoryImpl.this.saveCode(address, code);
        }

        @Override
        public byte[] getStorageHash() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void decode(byte[] rlpCode) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setDirty(boolean dirty) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setDeleted(boolean deleted) {
            RepositoryImpl.this.delete(address);
        }

        @Override
        public boolean isDirty() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public boolean isDeleted() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public byte[] getEncoded() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public int getStorageSize() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Set<DataWord> getStorageKeys() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Map<DataWord, DataWord> getStorage(@Nullable Collection<DataWord> keys) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Map<DataWord, DataWord> getStorage() {
            Source<DataWord, DataWord> storage = storageCache.get(address);
            CachedSourceImpl<DataWord, DataWord, byte[], byte[]> st = (CachedSourceImpl<DataWord, DataWord, byte[], byte[]>) storage;
            Map<DataWord, DataWord> ret = new HashMap<>();
            for (Map.Entry<DataWord, DataWord> entry : st.getCache().entrySet()) {
                if (entry.getValue() != null) {
                    ret.put(entry.getKey(), entry.getValue());
                }
            }
            return ret;
        }

        @Override
        public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setStorage(Map<DataWord, DataWord> storage) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public byte[] getAddress() {
            return address;
        }

        @Override
        public void setAddress(byte[] address) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public ContractDetails clone() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void syncStorage() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public ContractDetails getSnapshotTo(byte[] hash) {
            throw new RuntimeException("Not supported");
        }
    }


    @Override
    public Set<byte[]> getAccountsKeys() {
        CachedSourceImpl.BytesKey<AccountState, byte[]> cache = (CachedSourceImpl.BytesKey<AccountState, byte[]>) accountStateCache;
        Set<byte[]> ret = new HashSet<>();
        for (Map.Entry<byte[], AccountState> entry : cache.getCache().entrySet()) {
            if (entry.getValue() != null) ret.add(entry.getKey());
        }
        return ret;
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void flush() {
        throw new RuntimeException("Not supported");
    }


    @Override
    public void flushNoReconnect() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void syncToRoot(byte[] root) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public boolean isClosed() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void close() {
    }

    @Override
    public void reset() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public int getStorageSize(byte[] addr) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Set<DataWord> getStorageKeys(byte[] addr) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates, HashMap<ByteArrayWrapper, ContractDetails> contractDetailes) {
        for (Map.Entry<ByteArrayWrapper, AccountState> entry : accountStates.entrySet()) {
            accountStateCache.put(entry.getKey().getData(), entry.getValue());
        }
        for (Map.Entry<ByteArrayWrapper, ContractDetails> entry : contractDetailes.entrySet()) {
            ContractDetails details = getContractDetails(entry.getKey().getData());
            for (DataWord key : entry.getValue().getStorageKeys()) {
                details.put(key, entry.getValue().get(key));
            }
            byte[] code = entry.getValue().getCode();
            if (code != null && code.length > 0) {
                details.setCode(code);
            }
        }
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
        throw new RuntimeException("Not supported");
    }
}
