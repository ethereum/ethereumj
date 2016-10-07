package org.ethereum.datasource.test;

import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.Serializer;
import org.ethereum.util.RLP;
import org.ethereum.util.Value;
import org.ethereum.vm.DataWord;

/**
 * Created by Anton Nashatyrev on 05.10.2016.
 */
public class Test {

    class Account {
        byte[] address;
        AccountState accountState;
        ContractDetails contractDetails;
    }

    public class ContractDetails {

        AccountState accountState;

        Trie<byte[]> storageTrie;
        CachedSource<DataWord, DataWord, byte[], byte[]> storageCache;

        public ContractDetails(AccountState accountState, Trie<byte[]> storageTrie) {
            this.accountState = accountState;
            this.storageTrie = storageTrie;
            storageCache = new CachedSource<>(storageTrie, new WordSerializer(), new TrieWordSerializer());
        }

        public DataWord getStorage(DataWord addr) {
            return storageCache.get(addr);
        }

        public void setStorage(DataWord addr, DataWord value) {
            storageCache.put(addr, value);
        }
    }


    class Repository {
        Repository parent;

        CachedSource.Simple<byte[], byte[]> snapshotCache;
        CachedSource.BytesKey<Value, byte[]> trieCache;
        Trie<byte[]> trie;
        CachedSource.BytesKey<AccountState, byte[]> accountStateCache;

        public Repository(CachedSource.BytesKey<Value, byte[]> trieCache, byte[] root) {

        }
        public Repository(Source<byte[], byte[]> db, byte[] root) {
            snapshotCache = new CachedSource.Simple<>(db);
            snapshotCache.cacheReads = false;
            snapshotCache.cacheWrites = true;
            trieCache = new CachedSource.BytesKey<>(snapshotCache, new TrieCacheSerializer());
            trie = new TrieImpl(trieCache, root);
            accountStateCache = new CachedSource.BytesKey<>(trie,new AccountStateSerializer());
        }

        byte[] getCode(byte[] codeHash) {
            return snapshotCache.get(codeHash);
        }

        void putCode(byte[] code) {
            snapshotCache.put(HashUtil.sha3(code), code);
        }

        Account getAccount(byte[] addr) {
            Account ret = new Account();
            ret.address = addr;
            ret.accountState = accountStateCache.get(addr);
            ret.contractDetails = new ContractDetails(ret.accountState,
                    new TrieImpl(trieCache, ret.accountState.getStateRoot()));
            return ret;
        }

        void commit() {
            accountStateCache.flush();
//            contractDetails.flush();
            trieCache.flush();
            snapshotCache.flush();
        }

        Repository track() {
            throw new RuntimeException("TODO");
        }
    }

    Source<byte[], byte[]> stateDB;

    Repository getSnapshot(byte[] root) {
        return new Repository(stateDB, root);
    }

    class AccountStateSerializer implements Serializer<AccountState, byte[]> {
        @Override
        public byte[] serialize(AccountState object) {
            return object.getEncoded();
        }

        @Override
        public AccountState deserialize(byte[] stream) {
            return new AccountState(stream);
        }
    }

    class WordSerializer implements Serializer<DataWord, byte[]> {
        @Override
        public byte[] serialize(DataWord object) {
            return object.getData();
        }

        @Override
        public DataWord deserialize(byte[] stream) {
            return new DataWord(stream);
        }
    }

    class TrieWordSerializer implements Serializer<DataWord, byte[]> {
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

    class TrieCacheSerializer implements Serializer<Value, byte[]> {
        @Override
        public byte[] serialize(Value object) {
            return object.encode();
        }

        @Override
        public Value deserialize(byte[] stream) {
            return Value.fromRlpEncoded(stream);
        }
    }
}
