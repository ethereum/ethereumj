package org.ethereum.datasource.test;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Serializer;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.util.RLP;
import org.ethereum.util.Value;
import org.ethereum.vm.DataWord;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public class RepositoryNew implements Repository {

    private Source<byte[], AccountState> accountStateCache;
    private Source<byte[], byte[]> codeCache;
    private MultiCache<Source<DataWord, DataWord>> storageCache;

    public RepositoryNew(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,
                         MultiCache<Source<DataWord, DataWord>> storageCache) {
        this.accountStateCache = accountStateCache;
        this.codeCache = codeCache;
        this.storageCache = storageCache;
    }

    public static RepositoryNew createFromStateDS(Source<byte[], byte[]> stateDS, byte[] root) {
        CachedSource.Simple<byte[], byte[]> snapshotCache = new CachedSource.Simple<>(stateDS);
        final CachedSource.BytesKey<Value, byte[]> trieCache = new CachedSource.BytesKey<>
                (snapshotCache, new TrieCacheSerializer());
        trieCache.cacheReads = false;
        Trie<byte[]> trie = new TrieImpl(trieCache, root);
        final CachedSource.BytesKey<AccountState, byte[]> accountStateCache =
                new CachedSource.BytesKey<>(trie, new AccountStateSerializer());
        CachedSource.Simple<byte[], byte[]> codeCache = new CachedSource.Simple<>(snapshotCache);

        class TrieSource extends CachedSource<DataWord, DataWord, byte[], byte[]> {
            private Trie<byte[]> trie;
            public TrieSource(Trie<byte[]> src, Serializer<DataWord, byte[]> keySerializer, Serializer<DataWord, byte[]> valSerializer) {
                super(src, keySerializer, valSerializer);
            }
        }

        MultiCache<Source<DataWord, DataWord>> storageCache = new MultiCache<Source<DataWord, DataWord>>(null) {
            @Override
            protected Source<DataWord, DataWord> create(byte[] key, Source<DataWord, DataWord> srcCache) {
                AccountState accountState = accountStateCache.get(key);
                if (accountState == null) return null;
                TrieImpl storageTrie = new TrieImpl(trieCache, accountState.getStateRoot());
                CachedSource<DataWord, DataWord, byte[], byte[]> ret =
                        new CachedSource<>(storageTrie, new WordSerializer(), new TrieWordSerializer());
                return ret;
            }
        };
        return new RepositoryNew(accountStateCache, codeCache, storageCache) {
            @Override
            public void commit() {
                super.commit();
            }
        };
    }

    @Override
    public AccountState createAccount(byte[] addr) {
        AccountState state = new AccountState(BigInteger.ZERO, BigInteger.ZERO);
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

    @Override
    public void delete(byte[] addr) {
        accountStateCache.delete(addr);
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr).clone();
        accountState.incrementNonce();
        accountStateCache.put(addr, accountState);
        return accountState.getNonce();
    }

    @Override
    public BigInteger getNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? BigInteger.ZERO : accountState.getNonce();
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        // TODO
        return null;
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        // TODO
        return false;
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        byte[] codeHash = HashUtil.sha3(code);
        codeCache.put(codeHash, code);
        AccountState accountState = getAccountState(addr).clone();
        accountState.setCodeHash(codeHash);
        accountStateCache.put(addr, accountState);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? new byte[0] : codeCache.get(accountState.getCodeHash());
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        storageCache.get(addr).put(key, value);
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return storageCache.get(addr).get(key);
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState == null ? BigInteger.ZERO : accountState.getBalance();
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        AccountState accountState = getAccountState(addr).clone();
        accountState.addToBalance(value);
        accountStateCache.put(addr, accountState);
        return accountState.getBalance();
    }

    @Override
    public Repository startTracking() {
        CachedSource.Simple<byte[], AccountState> trackAccountStateCache = new CachedSource.Simple<>(accountStateCache);
        CachedSource.Simple<byte[], byte[]> trackCodeCache = new CachedSource.Simple<>(codeCache);
        MultiCache<Source<DataWord, DataWord>> trackStorageCache = new MultiCache<Source<DataWord, DataWord>>(storageCache) {
            @Override
            protected Source<DataWord, DataWord> create(byte[] key, Source<DataWord, DataWord> srcCache) {
                return new Simple<>(srcCache);
            }
        };

        return new RepositoryNew(trackAccountStateCache, trackCodeCache, trackStorageCache);
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
        // TODO
        throw new RuntimeException("TODO");
    }

    static class AccountStateSerializer implements Serializer<AccountState, byte[]> {
        @Override
        public byte[] serialize(AccountState object) {
            return object.getEncoded();
        }

        @Override
        public AccountState deserialize(byte[] stream) {
            return new AccountState(stream);
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


    @Override
    public Set<byte[]> getAccountsKeys() {
        throw new RuntimeException("Not supported");
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
        throw new RuntimeException("Not supported");
    }

    @Override
    public void reset() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates, HashMap<ByteArrayWrapper, ContractDetails> contractDetailes) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        throw new RuntimeException("Not supported");
    }
}
