package org.ethereum.db;

import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.util.HashMap;
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
    public void testExternalStorage() {
        DatabaseImpl db = new DatabaseImpl(new HashMapDB());
        DetailsDataStore dds = new DetailsDataStore();
        dds.setDB(db);

        byte[] addrWithExternalStorage = randomAddress();
        byte[] addrWithInternalStorage = randomAddress();
        final int inMemoryStorageLimit = 1000;

        HashMapDB externalStorage = new HashMapDB();
        HashMapDB internalStorage = new HashMapDB();

        ContractDetails details = randomContractDetails(512, inMemoryStorageLimit + 1, externalStorage);
        dds.update(addrWithExternalStorage, details);
        dds.update(addrWithInternalStorage, randomContractDetails(512, inMemoryStorageLimit - 1, internalStorage));
        
        dds.flush();
        
        assertTrue(externalStorage.getAddedItems() > 0);
        assertFalse(internalStorage.getAddedItems() > 0);

        ContractDetails detailsWithExternalStorage = dds.get(addrWithExternalStorage);
        assertNotNull(detailsWithExternalStorage);
        Map<DataWord, DataWord> storage = detailsWithExternalStorage.getStorage();
        assertNotNull(storage);
        assertEquals(inMemoryStorageLimit + 1, storage.size());

        ContractDetails detailsWithInternalStorage = dds.get(addrWithInternalStorage);
        assertNotNull(detailsWithInternalStorage);
        storage = detailsWithInternalStorage.getStorage();
        assertNotNull(storage);
        assertEquals(inMemoryStorageLimit - 1, storage.size());

        byte[] withExternalStorageRlp = detailsWithExternalStorage.getEncoded();
        ContractDetailsImpl decoded = new ContractDetailsImpl();
        decoded.setExternalStorageDataSource(externalStorage);
        decoded.decode(withExternalStorageRlp);
        
        assertEquals(inMemoryStorageLimit + 1, decoded.getStorage().size());


        assertTrue(withExternalStorageRlp.length < detailsWithInternalStorage.getEncoded().length);
    }
    
    @Test
    public void testStorageSerialization() {
        DatabaseImpl db = new DatabaseImpl(new HashMapDB());
        DetailsDataStore dds = new DetailsDataStore();
        dds.setDB(db);

        byte[] address = randomAddress();
        byte[] code = randomBytes(512);
        Map<DataWord, DataWord> elements = new HashMap<>();

        HashMapDB externalStorage = new HashMapDB();
        
        ContractDetailsImpl original = new ContractDetailsImpl();
        original.setExternalStorageDataSource(externalStorage);
        original.setAddress(address);
        original.setCode(code);
        
        final int inMemoryStorageLimit = 1000;
        for (int i = 0; i < inMemoryStorageLimit + 10; i++) {
            DataWord key = randomDataWord();
            DataWord value = randomDataWord();
            
            elements.put(key, value);
            original.put(key, value);
        }

        original.syncStorage();
        
        byte[] rlp = original.getEncoded();

        ContractDetailsImpl deserialized = new ContractDetailsImpl();
        deserialized.setExternalStorageDataSource(externalStorage);
        deserialized.decode(rlp);
        
        assertEquals(toHexString(address), toHexString(deserialized.getAddress()));
        assertEquals(toHexString(code), toHexString(deserialized.getCode()));

        Map<DataWord, DataWord> storage = deserialized.getStorage();
        assertEquals(elements.size(), storage.size());
        for (DataWord key : elements.keySet()) {
            assertEquals(elements.get(key), storage.get(key));
        }

        DataWord deletedKey = elements.keySet().iterator().next();

        deserialized.put(deletedKey, DataWord.ZERO);
        deserialized.put(randomDataWord(), DataWord.ZERO);

//        deserialized.
    }
    
    private static ContractDetails randomContractDetails(int codeSize, int storageSize, @Nullable KeyValueDataSource storageDataSource) {
        ContractDetailsImpl result = new ContractDetailsImpl();
        result.setCode(randomBytes(codeSize));
        if (storageDataSource != null) {
            result.setExternalStorageDataSource(storageDataSource);
        }

        for (int i = 0; i < storageSize; i++) {
            result.put(randomDataWord(), randomDataWord());
        }
        
        return result;
        
    }
}
