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
package org.ethereum.jsontestsuite.suite;

import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DbSource;
import org.ethereum.datasource.Source;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.db.ContractDetails;
import org.ethereum.trie.SecureTrie;
import org.ethereum.util.*;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.*;

/**
 * @author Roman Mandeleil
 * @since 24.06.2014
 */
@Component
@Scope("prototype")
public class ContractDetailsImpl extends AbstractContractDetails {
    private static final Logger logger = LoggerFactory.getLogger("general");

    CommonConfig commonConfig = CommonConfig.getDefault();

    SystemProperties config = SystemProperties.getDefault();

    DbSource dataSource;

    private byte[] address = EMPTY_BYTE_ARRAY;

    private Set<ByteArrayWrapper> keys = new HashSet<>();
    private SecureTrie storageTrie = new SecureTrie((byte[]) null);

    boolean externalStorage;
    private DbSource externalStorageDataSource;

    /** Tests only **/
    public ContractDetailsImpl() {
    }

    private ContractDetailsImpl(byte[] address, SecureTrie storageTrie, Map<ByteArrayWrapper, byte[]> codes) {
        this.address = address;
        this.storageTrie = storageTrie;
        setCodes(codes);
    }

    private void addKey(byte[] key) {
        keys.add(wrap(key));
    }

    private void removeKey(byte[] key) {
//        keys.remove(wrap(key)); // TODO: we can't remove keys , because of fork branching
    }

    @Override
    public void put(DataWord key, DataWord value) {
        if (value.equals(DataWord.ZERO)) {
            storageTrie.delete(key.getData());
            removeKey(key.getData());
        } else {
            storageTrie.put(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
            addKey(key.getData());
        }

        this.setDirty(true);
    }

    @Override
    public DataWord get(DataWord key) {
        DataWord result = null;

        byte[] data = storageTrie.get(key.getData());
        if (data.length > 0) {
            byte[] dataDecoded = RLP.decode2(data).get(0).getRLPData();
            result = new DataWord(dataDecoded);
        }

        return result;
    }

    @Override
    public byte[] getStorageHash() {
        return storageTrie.getRootHash();
    }

    @Override
    public void decode(byte[] rlpCode) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public byte[] getEncoded() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Map<DataWord, DataWord> getStorage(Collection<DataWord> keys) {
        Map<DataWord, DataWord> storage = new HashMap<>();
        if (keys == null) {
            for (ByteArrayWrapper keyBytes : this.keys) {
                DataWord key = new DataWord(keyBytes);
                DataWord value = get(key);

                // we check if the value is not null,
                // cause we keep all historical keys
                if (value != null)
                    storage.put(key, value);
            }
        } else {
            for (DataWord key : keys) {
                DataWord value = get(key);

                // we check if the value is not null,
                // cause we keep all historical keys
                if (value != null)
                    storage.put(key, value);
            }
        }

        return storage;
    }

    @Override
    public Map<DataWord, DataWord> getStorage() {
        return getStorage(null);
    }

    @Override
    public int getStorageSize() {
        return keys.size();
    }

    @Override
    public Set<DataWord> getStorageKeys() {
        Set<DataWord> result = new HashSet<>();
        for (ByteArrayWrapper key : keys) {
            result.add(new DataWord(key));
        }
        return result;
    }

    @Override
    public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {

        for (int i = 0; i < storageKeys.size(); ++i)
            put(storageKeys.get(i), storageValues.get(i));
    }

    @Override
    public void setStorage(Map<DataWord, DataWord> storage) {
        for (DataWord key : storage.keySet()) {
            put(key, storage.get(key));
        }
    }

    @Override
    public byte[] getAddress() {
        return address;
    }

    @Override
    public void setAddress(byte[] address) {
        this.address = address;
    }

    public SecureTrie getStorageTrie() {
        return storageTrie;
    }

    @Override
    public void syncStorage() {
    }

    @Override
    public ContractDetails clone() {

        // FIXME: clone is not working now !!!
        // FIXME: should be fixed

//        storageTrie.getRoot();

        return new ContractDetailsImpl(address, null, getCodes());
    }

    @Override
    public ContractDetails getSnapshotTo(byte[] hash){

        Source<byte[], byte[]> cache = this.storageTrie.getCache();

        SecureTrie snapStorage = wrap(hash).equals(wrap(EMPTY_TRIE_HASH)) ?
            new SecureTrie(cache, "".getBytes()):
            new SecureTrie(cache, hash);

        ContractDetailsImpl details = new ContractDetailsImpl(this.address, snapStorage, getCodes());
        details.externalStorage = this.externalStorage;
        details.externalStorageDataSource = this.externalStorageDataSource;
        details.keys = this.keys;
        details.config = config;
        details.commonConfig = commonConfig;
        details.dataSource = dataSource;

        return details;
    }
}

