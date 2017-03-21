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

//    private static class StorageCache extends ReadWriteCache<DataWord, DataWord> {
    private static class StorageCache extends ReadWriteCache<DataWord, DataWord> {
        byte[] accountAddress;
        Trie<byte[]> trie;

        public StorageCache(byte[] accountAddress, Trie<byte[]> trie) {
//            super(new SourceCodec<>(trie, Serializers.StorageKeySerializer, Serializers.StorageValueSerializer),
//                    WriteCache.CacheType.SIMPLE);
            super(new SourceCodec<>(trie, Serializers.StorageKeySerializer, Serializers.StorageValueSerializer), WriteCache.CacheType.SIMPLE);
            this.accountAddress = accountAddress;
            this.trie = trie;
        }
    }

    private class MultiStorageCache extends MultiCache<StorageCache> {
        public MultiStorageCache() {
            super(null);
        }
        @Override
        protected synchronized StorageCache create(byte[] key, StorageCache srcCache) {
            AccountState accountState = accountStateCache.get(key);
            TrieImpl storageTrie = createTrie(trieCache, accountState == null ? null : accountState.getStateRoot());
            return new StorageCache(key, storageTrie);
        }

        @Override
        protected synchronized boolean flushChild(StorageCache childCache) {
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
    private CachedSource.BytesKey<Value> trieCache;
    private TrieImpl stateTrie;

    public RepositoryRoot(Source<byte[], byte[]> stateDS) {
        this(stateDS, null);
    }

    /**
     * Building the following structure for snapshot Repository:
     *
     * stateDS --> trieCacheCodec --> trieCache --> stateTrie --> accountStateCodec --> accountStateCache
     *  \                               \
     *   \                               \-->>>  contractStorageTrie --> storageCodec --> StorageCache
     *    \--> codeCache
     *
     *
     * @param stateDS
     * @param root
     */
    public RepositoryRoot(final Source<byte[], byte[]> stateDS, byte[] root) {
        this.stateDS = stateDS;

        SourceCodec.BytesKey<Value, byte[]> trieCacheCodec = new SourceCodec.BytesKey<>(stateDS, Serializers.TrieNodeSerializer);
        trieCache = new WriteCache.BytesKey<>(trieCacheCodec, WriteCache.CacheType.COUNTING);
        stateTrie = createTrie(trieCache, root);

        SourceCodec.BytesKey<AccountState, byte[]> accountStateCodec = new SourceCodec.BytesKey<>(stateTrie, Serializers.AccountStateSerializer);
//        final CachedSource.BytesKey<AccountState> accountStateCache = new CachedSourceImpl.BytesKey<>(accountStateCodec);
        final ReadWriteCache.BytesKey<AccountState> accountStateCache = new ReadWriteCache.BytesKey<>(accountStateCodec, WriteCache.CacheType.SIMPLE);

        final MultiCache<StorageCache> storageCache = new MultiStorageCache();

        // counting as there can be 2 contracts with the same code, 1 can suicide
        Source<byte[], byte[]> codeCache = new WriteCache.BytesKey<>(stateDS, WriteCache.CacheType.COUNTING);

        init(accountStateCache, codeCache, storageCache);
    }

    @Override
    public synchronized void commit() {
        super.commit();

        trieCache.flush();
    }

    @Override
    public synchronized byte[] getRoot() {
        storageCache.flush();
        accountStateCache.flush();

        return stateTrie.getRootHash();
    }

    @Override
    public synchronized void flush() {
        commit();
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        return new RepositoryRoot(stateDS, root);
    }

    @Override
    public synchronized String dumpStateTrie() {
        return stateTrie.getTrieDump();
    }

    @Override
    public synchronized void syncToRoot(byte[] root) {
        stateTrie.setRoot(root);
    }

    @Override
    public synchronized Value getState(byte[] stateRoot) {
        return trieCache.get(stateRoot);
    }

    protected TrieImpl createTrie(CachedSource.BytesKey<Value> trieCache, byte[] root) {
        return new SecureTrie(trieCache, root);
    }

    @Override
    public synchronized void addRawNode(byte[] key, byte[] value) {
        trieCache.put(key, Value.fromRlpEncoded(value));
    }
}
