package org.ethereum.db;

import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DataSourcePool;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.trie.SecureTrie;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ethereum.util.ByteUtil.*;

/**
 * @author Roman Mandeleil
 * @since 24.06.2014
 */
@Component @Scope("prototype")
public class ContractDetailsImpl extends AbstractContractDetails {
    private static final Logger logger = LoggerFactory.getLogger("general");

    @Autowired
    CommonConfig commonConfig = CommonConfig.getDefault();

    @Autowired
    SystemProperties config = SystemProperties.getDefault();

    @Autowired
    DataSourcePool dataSourcePool = DataSourcePool.getDefault();

    private byte[] rlpEncoded;

    private byte[] address = EMPTY_BYTE_ARRAY;

    private Set<ByteArrayWrapper> keys = new HashSet<>();
    private SecureTrie storageTrie = new SecureTrie(null);

    boolean externalStorage;
    private KeyValueDataSource externalStorageDataSource;

    public ContractDetailsImpl() {
    }

    public ContractDetailsImpl(byte[] rlpCode) {
        decode(rlpCode);
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
            storageTrie.update(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
            addKey(key.getData());
        }

        this.setDirty(true);
        this.rlpEncoded = null;
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
        RLPList data = RLP.decode2(rlpCode);
        RLPList rlpList = (RLPList) data.get(0);

        RLPItem address = (RLPItem) rlpList.get(0);
        RLPItem isExternalStorage = (RLPItem) rlpList.get(1);
        RLPItem storage = (RLPItem) rlpList.get(2);
        RLPElement code = rlpList.get(3);
        RLPList keys = (RLPList) rlpList.get(4);
        RLPItem storageRoot = (RLPItem) rlpList.get(5);

        this.address = address.getRLPData();
        this.externalStorage = (isExternalStorage.getRLPData() != null);
        this.storageTrie.deserialize(storage.getRLPData());
        if (code instanceof RLPList) {
            for (RLPElement e : ((RLPList) code)) {
                setCode(e.getRLPData());
            }
        } else {
            setCode(code.getRLPData());
        }
        for (RLPElement key : keys) {
            addKey(key.getRLPData());
        }

        if (externalStorage) {
            storageTrie.setRoot(storageRoot.getRLPData());
            storageTrie.getCache().setDB(getExternalStorageDataSource());
        }

        externalStorage = (storage.getRLPData().length > config.detailsInMemoryStorageLimit())
                || externalStorage;

        this.rlpEncoded = rlpCode;
    }

    @Override
    public byte[] getEncoded() {
        if (rlpEncoded == null) {

            byte[] rlpAddress = RLP.encodeElement(address);
            byte[] rlpIsExternalStorage = RLP.encodeByte((byte) (externalStorage ? 1 : 0));
            byte[] rlpStorageRoot = RLP.encodeElement(externalStorage ? storageTrie.getRootHash() : EMPTY_BYTE_ARRAY);
            byte[] rlpStorage = RLP.encodeElement(storageTrie.serialize());
            byte[][] codes = new byte[getCodes().size()][];
            int i = 0;
            for (byte[] bytes : this.getCodes().values()) {
                codes[i++] = RLP.encodeElement(bytes);
            }
            byte[] rlpCode = RLP.encodeList(codes);
            byte[] rlpKeys = RLP.encodeSet(keys);

            this.rlpEncoded = RLP.encodeList(rlpAddress, rlpIsExternalStorage, rlpStorage, rlpCode, rlpKeys, rlpStorageRoot);
        }

        return rlpEncoded;
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
        this.rlpEncoded = null;
    }

    @Override
    public void syncStorage() {
        if (externalStorage) {
            storageTrie.getCache().setDB(getExternalStorageDataSource());
            storageTrie.sync();
            dataSourcePool.closeDataSource("details-storage/" + toHexString(address));
        }
    }

    private KeyValueDataSource getExternalStorageDataSource() {
        if (externalStorageDataSource == null) {
            externalStorageDataSource = dataSourcePool.dbByName(commonConfig, "details-storage/" + toHexString(address));
        }
        return externalStorageDataSource;
    }

    public void setExternalStorageDataSource(KeyValueDataSource dataSource) {
        this.externalStorageDataSource = dataSource;
    }

    @Override
    public ContractDetails clone() {

        // FIXME: clone is not working now !!!
        // FIXME: should be fixed

        storageTrie.getRoot();

        return new ContractDetailsImpl(address, null, getCodes());
    }

    @Override
    public ContractDetails getSnapshotTo(byte[] hash){

        KeyValueDataSource keyValueDataSource = this.storageTrie.getCache().getDb();

        SecureTrie snapStorage = wrap(hash).equals(wrap(EMPTY_TRIE_HASH)) ?
            new SecureTrie(keyValueDataSource, "".getBytes()):
            new SecureTrie(keyValueDataSource, hash);


        snapStorage.setCache(this.storageTrie.getCache());

        ContractDetailsImpl details = new ContractDetailsImpl(this.address, snapStorage, getCodes());
        details.externalStorage = this.externalStorage;
        details.externalStorageDataSource = this.externalStorageDataSource;
        details.keys = this.keys;
        details.config = config;
        details.commonConfig = commonConfig;
        details.dataSourcePool = dataSourcePool;

        return details;
    }
}

