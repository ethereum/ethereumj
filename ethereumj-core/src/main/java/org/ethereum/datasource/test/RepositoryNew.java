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

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public class RepositoryNew implements Repository {

    private Source<byte[], AccountState> accountStateCache;
    private Source<byte[], byte[]> codeCache;
    private MultiCache<? extends Source<DataWord, DataWord>> storageCache;

    public RepositoryNew(Source<byte[], AccountState> accountStateCache, Source<byte[], byte[]> codeCache,
                         MultiCache<? extends Source<DataWord, DataWord>> storageCache) {
        this.accountStateCache = accountStateCache;
        this.codeCache = codeCache;
        this.storageCache = storageCache;
    }

    public static RepositoryNew createFromStateDS(Source<byte[], byte[]> stateDS, byte[] root) {
        final CachedSource.Simple<byte[], byte[]> snapshotCache = new CachedSource.Simple<>(stateDS);

        final CachedSource.BytesKey<Value, byte[]> trieCache = new CachedSource.BytesKey<>
                (snapshotCache, new TrieCacheSerializer());
        trieCache.cacheReads = false;
        trieCache.noDelete = true;
        final TrieImpl trie = new TrieImpl(trieCache, root);

        final CachedSource.BytesKey<AccountState, byte[]> accountStateCache =
                new CachedSource.BytesKey<>(trie, new AccountStateSerializer());
        CachedSource.Simple<byte[], byte[]> codeCache = new CachedSource.Simple<>(snapshotCache);

        class MultiTrieCache extends CachedSource<DataWord, DataWord, byte[], byte[]> {
            byte[] accountAddress;
            Trie<byte[]> trie;

            public MultiTrieCache(byte[] accountAddress, Trie<byte[]> trie) {
                super(trie, new WordSerializer(), new TrieWordSerializer());
                this.accountAddress = accountAddress;
                this.trie = trie;
            }
        }

        final MultiCache<MultiTrieCache> storageCache = new MultiCache<MultiTrieCache>(null) {
            @Override
            protected MultiTrieCache create(byte[] key, MultiTrieCache srcCache) {
                AccountState accountState = accountStateCache.get(key);
                if (accountState == null) return null;
                TrieImpl storageTrie = new TrieImpl(trieCache, accountState.getStateRoot());
                return new MultiTrieCache(key, storageTrie);
            }

            @Override
            protected void flushChild(MultiTrieCache childCache) {
                super.flushChild(childCache);
//                RepositoryNew.this.updateAccountStateRoot(childCache.accountAddress, childCache.trie.getRootHash());
                byte[] rootHash = childCache.trie.getRootHash();
                AccountState state = accountStateCache.get(childCache.accountAddress).clone();
                state.setStateRoot(rootHash);
                accountStateCache.put(childCache.accountAddress, state);
            }
        };
        return new RepositoryNew(accountStateCache, codeCache, storageCache) {
            @Override
            public void commit() {
                super.commit();

                trieCache.flush();
                snapshotCache.flush();
            }

            @Override
            public byte[] getRoot() {
                return trie.getRootHash();
            }

            @Override
            public String dumpStateTrie() {
                return trie.getTrieDump();
            }
        };
    }

    public String dumpStateTrie() {
        throw new RuntimeException("Not supported");
    }

    private void updateAccountStateRoot(byte[] addr, byte[] root) {
        AccountState accountState = getAccountState(addr).clone();
        accountState.setStateRoot(root);
        accountStateCache.put(addr, accountState);
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
    public RepositoryNew startTracking() {
        CachedSource.Simple<byte[], AccountState> trackAccountStateCache = new CachedSource.Simple<>(accountStateCache);
        CachedSource.Simple<byte[], byte[]> trackCodeCache = new CachedSource.Simple<>(codeCache);
        MultiCache<? extends Source<DataWord, DataWord>> trackStorageCache = new MultiCache(storageCache) {
            @Override
            protected Source create(byte[] key, Source srcCache) {
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
            RepositoryNew.this.addStorageRow(address, key, value);
        }

        @Override
        public DataWord get(DataWord key) {
            return RepositoryNew.this.getStorageValue(address, key);
        }

        @Override
        public byte[] getCode() {
            return RepositoryNew.this.getCode(address);
        }

        @Override
        public byte[] getCode(byte[] codeHash) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setCode(byte[] code) {
            RepositoryNew.this.saveCode(address, code);
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
            RepositoryNew.this.delete(address);
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
            throw new RuntimeException("Not supported");
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
