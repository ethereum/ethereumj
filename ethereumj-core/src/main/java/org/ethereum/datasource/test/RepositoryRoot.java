package org.ethereum.datasource.test;

import org.ethereum.config.CommonConfig;
import org.ethereum.core.AccountState;
import org.ethereum.core.Repository;
import org.ethereum.util.Value;
import org.ethereum.vm.DataWord;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public class RepositoryRoot extends RepositoryImpl {

    private static class StorageCache extends CachedSourceImpl<DataWord, DataWord, byte[], byte[]> {
        byte[] accountAddress;
        Trie<byte[]> trie;

        public StorageCache(byte[] accountAddress, Trie<byte[]> trie) {
            super(trie, new WordSerializer(), new TrieWordSerializer());
            this.accountAddress = accountAddress;
            this.trie = trie;
        }
    }

    private class MultiStorageCache extends MultiCache<StorageCache> {
        public MultiStorageCache() {
            super(null);
        }
        @Override
        protected StorageCache create(byte[] key, StorageCache srcCache) {
            AccountState accountState = accountStateCache.get(key);
            TrieImpl storageTrie = createTrie(trieCache, accountState == null ? null : accountState.getStateRoot());
            return new StorageCache(key, storageTrie);
        }

        @Override
        protected boolean flushChild(StorageCache childCache) {
            if (super.flushChild(childCache)) {
                AccountState storageOwnerAcct = accountStateCache.get(childCache.accountAddress);
                if (storageOwnerAcct != null) {
                    // need to update account storage root
                    byte[] rootHash = childCache.trie.getRootHash();
                    AccountState state = storageOwnerAcct.clone();
                    state.setStateRoot(rootHash);
                    accountStateCache.put(childCache.accountAddress, state);
                    return true;
                } else {
                    // account was deleted
                    return false;
                }
            } else {
                // no storage changes
                return false;
            }
        }
    }

    private Source<byte[], byte[]> stateDS;
    private CachedSource.SimpleBytesKey<byte[]> snapshotCache;
    private CachedSource.BytesKey<Value, byte[]> trieCache;
    private TrieImpl trie;

    @Autowired
    CommonConfig commonConfig = CommonConfig.getDefault();

    public RepositoryRoot(Source<byte[], byte[]> stateDS) {
        this(stateDS, null);
    }

    public RepositoryRoot(final Source<byte[], byte[]> stateDS, byte[] root) {
        this.stateDS = stateDS;
        snapshotCache = new CachedSourceImpl.SimpleBytesKey<>(stateDS);

        trieCache = new CachedSourceImpl.BytesKey<Value, byte[]>
                (snapshotCache, new TrieCacheSerializer()) {{
                withCacheReads(false);
                withNoDelete(true);
            }};
        trie = createTrie(trieCache, root);

        final CachedSource.BytesKey<AccountState, byte[]> accountStateCache =
                new CachedSourceImpl.BytesKey<>(trie, new AccountStateSerializer());
        CachedSource.SimpleBytesKey<byte[]> codeCache = new CachedSourceImpl.SimpleBytesKey<>(snapshotCache);

        final MultiCache<StorageCache> storageCache = new MultiStorageCache();

        init(accountStateCache, codeCache, storageCache);
    }

    @Override
    public void commit() {
        super.commit();

        trieCache.flush();
        snapshotCache.flush();
    }

    @Override
    public byte[] getRoot() {
        super.commit();
        return trie.getRootHash();
    }

    @Override
    public void flush() {
        commit();
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        return new RepositoryRoot(stateDS, root);
    }

    @Override
    public String dumpStateTrie() {
        return trie.getTrieDump();
    }

    @Override
    public void syncToRoot(byte[] root) {
        trie.setRoot(root);
    }

    @Override
    public Value getState(byte[] stateRoot) {
        return trieCache.get(stateRoot);
    }

    protected TrieImpl createTrie(CachedSource.BytesKey<Value, byte[]> trieCache, byte[] root) {
        return new SecureTrie(trieCache, root);
    }
}
