package org.ethereum.db;

import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.XorDataSource;
import org.ethereum.trie.SecureTrie;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

//    private byte[] rlpEncoded;

    private byte[] address = EMPTY_BYTE_ARRAY;

//    private Set<ByteArrayWrapper> keys = new HashSet<>();
    private SecureTrie storageTrie;

    /** Tests only **/
    public ContractDetailsImpl() {
    }

    public ContractDetailsImpl(final CommonConfig commonConfig, final SystemProperties config) {
        this.commonConfig = commonConfig;
        this.config = config;
    }

    /** Tests only **/
    public ContractDetailsImpl(byte[] rlpCode) {
        decode(rlpCode);
    }

    private ContractDetailsImpl(byte[] address, SecureTrie storageTrie, Map<ByteArrayWrapper, byte[]> codes) {
        this.address = address;
        this.storageTrie = storageTrie;
//        setCodes(codes);
    }

    public void setAccountState(AccountState accountState) {
        this.accountState = accountState;
        storageTrie.getCache().setDB(dataSource);
    }

    @Override
    public void put(DataWord key, DataWord value) {
        if (value.equals(DataWord.ZERO)) {
            storageTrie.delete(key.getData());
        } else {
            storageTrie.update(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
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
        throw new RuntimeException("Not supported");
    }

    @Override
    public Map<DataWord, DataWord> getStorage() {
        return getStorage(null);
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
//        if (externalStorage) {
//            storageTrie.withPruningEnabled(config.databasePruneDepth() >= 0);
//            storageTrie.getCache().setDB(getExternalStorageDataSource());
//            storageTrie.sync();
//        }
    }

    public void setDataSource(KeyValueDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public ContractDetails clone() {
        throw new RuntimeException("Not supported");
//        // FIXME: clone is not working now !!!
//        // FIXME: should be fixed
//
//        storageTrie.getRoot();
//
//        return new ContractDetailsImpl(address, null, getCodes());
    }

    @Override
    public ContractDetails getSnapshotTo(byte[] hash){

        KeyValueDataSource keyValueDataSource = this.storageTrie.getCache().getDb();

        SecureTrie snapStorage = wrap(hash).equals(wrap(EMPTY_TRIE_HASH)) ?
            new SecureTrie(keyValueDataSource, "".getBytes()):
            new SecureTrie(keyValueDataSource, hash);
        snapStorage.withPruningEnabled(storageTrie.isPruningEnabled());

        snapStorage.setCache(this.storageTrie.getCache());

        ContractDetailsImpl details = new ContractDetailsImpl(this.address, snapStorage, null);
        details.config = config;
        details.commonConfig = commonConfig;
        details.dataSource = dataSource;

        return details;
    }
}

