package org.ethereum.db;

import org.ethereum.core.AccountState;
import org.ethereum.core.Repository;
import org.ethereum.datasource.*;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.ethereum.trie.TrieImpl;
import org.ethereum.util.Value;
import org.ethereum.vm.DataWord;

/**
 * Created by Anton Nashatyrev on 07.10.2016.
 */
public class RepositoryRoot extends RepositoryImpl {

    private static class StorageCache extends CachedSourceImpl<DataWord, DataWord> {
        byte[] accountAddress;
        Trie<byte[]> trie;

        public StorageCache(byte[] accountAddress, Trie<byte[]> trie) {
            super(new SourceCodec<>(trie, Serializers.WordSerializer, Serializers.TrieWordSerializer));
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
                    accountStateCache.put(childCache.accountAddress, storageOwnerAcct.withStateRoot(rootHash));
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
    private CachedSource.BytesKey<byte[]> snapshotCache;
    private CachedSource.BytesKey<Value> trieCache;
    private TrieImpl stateTrie;

    public RepositoryRoot(Source<byte[], byte[]> stateDS) {
        this(stateDS, null);
    }

    public RepositoryRoot(final Source<byte[], byte[]> stateDS, byte[] root) {
        this.stateDS = stateDS;
        snapshotCache = new CachedSourceImpl.BytesKey<>(stateDS);

        SourceCodec.BytesKey<Value, byte[]> trieCacheCodec = new SourceCodec.BytesKey<>(snapshotCache, Serializers.TrieCacheSerializer);
        trieCache = new CachedSourceImpl.BytesKey<Value>(trieCacheCodec) {{
                withCacheReads(false);
                withNoDelete(true);
            }};
        stateTrie = createTrie(trieCache, root);

        SourceCodec.BytesKey<AccountState, byte[]> accountStateCodec = new SourceCodec.BytesKey<>(stateTrie, Serializers.AccountStateSerializer);
        final CachedSource.BytesKey<AccountState> accountStateCache = new CachedSourceImpl.BytesKey<>(accountStateCodec);
        CachedSource.BytesKey<byte[]> codeCache = new CachedSourceImpl.BytesKey<>(snapshotCache);

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
        return stateTrie.getRootHash();
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
        return stateTrie.getTrieDump();
    }

    @Override
    public void syncToRoot(byte[] root) {
        stateTrie.setRoot(root);
    }

    @Override
    public Value getState(byte[] stateRoot) {
        return trieCache.get(stateRoot);
    }

    protected TrieImpl createTrie(CachedSource.BytesKey<Value> trieCache, byte[] root) {
        return new SecureTrie(trieCache, root);
    }
}
