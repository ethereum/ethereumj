package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DataSourcePool;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.util.Map;

import static org.ethereum.TestUtils.*;
import static org.ethereum.util.ByteUtil.toHexString;
import static org.junit.Assert.*;

public class DetailsDataStoreTest {

    @Test
    public void test1(){

        DatabaseImpl db = new DatabaseImpl(new HashMapDB());
        DetailsDataStore dds = new DetailsDataStore();
        dds.setDB(db);

        byte[] c_key = Hex.decode("1a2b");
        byte[] code = Hex.decode("60606060");
        byte[] key =  Hex.decode("11");
        byte[] value =  Hex.decode("aa");

        ContractDetails contractDetails = new ContractDetailsImpl();
        contractDetails.setAddress(randomAddress());
        contractDetails.setCode(code);
        contractDetails.put(new DataWord(key), new DataWord(value));

        dds.update(c_key, contractDetails);

        ContractDetails contractDetails_ = dds.get(c_key);

        String encoded1 = Hex.toHexString(contractDetails.getEncoded());
        String encoded2 = Hex.toHexString(contractDetails_.getEncoded());

        assertEquals(encoded1, encoded2);

        dds.flush();

        contractDetails_ = dds.get(c_key);
        encoded2 = Hex.toHexString(contractDetails_.getEncoded());
        assertEquals(encoded1, encoded2);
    }

    @Test
    public void test2(){

        DatabaseImpl db = new DatabaseImpl(new HashMapDB());
        DetailsDataStore dds = new DetailsDataStore();
        dds.setDB(db);

        byte[] c_key = Hex.decode("1a2b");
        byte[] code = Hex.decode("60606060");
        byte[] key =  Hex.decode("11");
        byte[] value =  Hex.decode("aa");

        ContractDetails contractDetails = new ContractDetailsImpl();
        contractDetails.setCode(code);
        contractDetails.setAddress(randomAddress());
        contractDetails.put(new DataWord(key), new DataWord(value));

        dds.update(c_key, contractDetails);

        ContractDetails contractDetails_ = dds.get(c_key);

        String encoded1 = Hex.toHexString(contractDetails.getEncoded());
        String encoded2 = Hex.toHexString(contractDetails_.getEncoded());

        assertEquals(encoded1, encoded2);

        dds.remove(c_key);

        contractDetails_ = dds.get(c_key);
        assertNull(contractDetails_);

        dds.flush();

        contractDetails_ = dds.get(c_key);
        assertNull(contractDetails_);
    }

    @Test
    public void test3(){

        DatabaseImpl db = new DatabaseImpl(new HashMapDB());
        DetailsDataStore dds = new DetailsDataStore();
        dds.setDB(db);

        byte[] c_key = Hex.decode("1a2b");
        byte[] code = Hex.decode("60606060");
        byte[] key =  Hex.decode("11");
        byte[] value =  Hex.decode("aa");

        ContractDetails contractDetails = new ContractDetailsImpl();
        contractDetails.setCode(code);
        contractDetails.put(new DataWord(key), new DataWord(value));

        dds.update(c_key, contractDetails);

        ContractDetails contractDetails_ = dds.get(c_key);

        String encoded1 = Hex.toHexString(contractDetails.getEncoded());
        String encoded2 = Hex.toHexString(contractDetails_.getEncoded());

        assertEquals(encoded1, encoded2);

        dds.remove(c_key);
        dds.update(c_key, contractDetails);

        contractDetails_ = dds.get(c_key);
        encoded2 = Hex.toHexString(contractDetails_.getEncoded());
        assertEquals(encoded1, encoded2);

        dds.flush();

        contractDetails_ = dds.get(c_key);
        encoded2 = Hex.toHexString(contractDetails_.getEncoded());
        assertEquals(encoded1, encoded2);
    }

    @Test
    public void test4() {

        DatabaseImpl db = new DatabaseImpl(new HashMapDB());
        DetailsDataStore dds = new DetailsDataStore();
        dds.setDB(db);

        byte[] c_key = Hex.decode("1a2b");

        ContractDetails contractDetails = dds.get(c_key);
        assertNull(contractDetails);
    }

    @Test
    public void testExternalStorage() throws InterruptedException {
        DatabaseImpl db = new DatabaseImpl(new HashMapDB());
        DetailsDataStore dds = new DetailsDataStore();
        dds.setDB(db);

        byte[] addrWithExternalStorage = randomAddress();
        byte[] addrWithInternalStorage = randomAddress();
        final int inMemoryStorageLimit = SystemProperties.getDefault().detailsInMemoryStorageLimit();

        DataSourcePool dataSourcePool = DataSourcePool.getDefault();
        HashMapDB externalStorage =
            (HashMapDB) dataSourcePool.hashMapDBByName("details-storage/" + toHexString(addrWithExternalStorage));

        HashMapDB internalStorage = new HashMapDB();

        ContractDetails detailsWithExternalStorage = randomContractDetails(512, inMemoryStorageLimit / 64 + 10, externalStorage, true);
        ContractDetails detailsWithInternalStorage = randomContractDetails(512, inMemoryStorageLimit / 64 - 10, internalStorage, false);

        DataWord key = detailsWithExternalStorage.getStorageKeys().iterator().next();

        dds.update(addrWithExternalStorage, detailsWithExternalStorage);
        dds.update(addrWithInternalStorage, detailsWithInternalStorage);

        dds.flush();

        assertTrue(externalStorage.getAddedItems() > 0);
        assertFalse(internalStorage.getAddedItems() > 0);

        detailsWithExternalStorage = dds.get(addrWithExternalStorage);

        assertNotNull(detailsWithExternalStorage);

        Map<DataWord, DataWord> storage = detailsWithExternalStorage.getStorage();
        assertNotNull(storage);
        assertEquals(inMemoryStorageLimit / 64 + 10, storage.size());

        byte[] withExternalStorageRlp = detailsWithExternalStorage.getEncoded();
        ContractDetailsImpl decoded = new ContractDetailsImpl();
        decoded.setExternalStorageDataSource(externalStorage);
        decoded.decode(withExternalStorageRlp);

        assertEquals(inMemoryStorageLimit / 64 + 10, decoded.getStorage().size());
        assertTrue(withExternalStorageRlp.length < detailsWithInternalStorage.getEncoded().length);


        detailsWithInternalStorage = dds.get(addrWithInternalStorage);
        assertNotNull(detailsWithInternalStorage);
        storage = detailsWithInternalStorage.getStorage();
        assertNotNull(storage);
        assertEquals(inMemoryStorageLimit / 64 - 10, storage.size());

        // from inmemory to ondisk transition checking
        externalStorage = new HashMapDB();
        ((ContractDetailsImpl) detailsWithInternalStorage).setExternalStorageDataSource(externalStorage);
        detailsWithInternalStorage.put(randomDataWord(), randomDataWord());
    }

    private static ContractDetails randomContractDetails(int codeSize, int storageSize, @Nullable KeyValueDataSource storageDataSource,
                                                         boolean external) {
        ContractDetailsImpl result = new ContractDetailsImpl();
        result.externalStorage = external;
        result.setCode(randomBytes(codeSize));

        if (storageDataSource != null)
            result.setExternalStorageDataSource(storageDataSource);

        for (int i = 0; i < storageSize; i++) {
            result.put(randomDataWord(), randomDataWord());
        }

        return result;
    }
}
