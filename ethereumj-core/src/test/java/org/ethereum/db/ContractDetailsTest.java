package org.ethereum.db;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.HashMap;
import java.util.Map;

import static org.ethereum.TestUtils.randomAddress;
import static org.ethereum.TestUtils.randomBytes;
import static org.ethereum.TestUtils.randomDataWord;
import static org.ethereum.util.ByteUtil.toHexString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContractDetailsTest {

    private static final int IN_MEMORY_STORAGE_LIMIT = SystemProperties.getDefault().detailsInMemoryStorageLimit();

    @Test
    public void test_1(){

        byte[] code = Hex.decode("60016002");

        byte[] key_1 = Hex.decode("111111");
        byte[] val_1 = Hex.decode("aaaaaa");

        byte[] key_2 = Hex.decode("222222");
        byte[] val_2 = Hex.decode("bbbbbb");

        ContractDetailsImpl contractDetails = new ContractDetailsImpl();
        contractDetails.setCode(code);
        contractDetails.put(new DataWord(key_1), new DataWord(val_1));
        contractDetails.put(new DataWord(key_2), new DataWord(val_2));

        byte[] data = contractDetails.getEncoded();

        ContractDetailsImpl contractDetails_ = new ContractDetailsImpl(data);

        assertEquals(Hex.toHexString(code),
            Hex.toHexString(contractDetails_.getCode()));

        assertEquals(Hex.toHexString(val_1),
                Hex.toHexString(contractDetails_.get(new DataWord(key_1)).getNoLeadZeroesData()));

        assertEquals(Hex.toHexString(val_2),
                Hex.toHexString(contractDetails_.get(new DataWord(key_2)).getNoLeadZeroesData()));
    }

    @Test
    public void test_2(){

        byte[] code = Hex.decode("7c0100000000000000000000000000000000000000000000000000000000600035046333d546748114610065578063430fe5f01461007c5780634d432c1d1461008d578063501385b2146100b857806357eb3b30146100e9578063dbc7df61146100fb57005b6100766004356024356044356102f0565b60006000f35b61008760043561039e565b60006000f35b610098600435610178565b8073ffffffffffffffffffffffffffffffffffffffff1660005260206000f35b6100c96004356024356044356101a0565b8073ffffffffffffffffffffffffffffffffffffffff1660005260206000f35b6100f1610171565b8060005260206000f35b610106600435610133565b8360005282602052816040528073ffffffffffffffffffffffffffffffffffffffff1660605260806000f35b5b60006020819052908152604090208054600182015460028301546003909301549192909173ffffffffffffffffffffffffffffffffffffffff1684565b5b60015481565b5b60026020526000908152604090205473ffffffffffffffffffffffffffffffffffffffff1681565b73ffffffffffffffffffffffffffffffffffffffff831660009081526020819052604081206002015481908302341080156101fe575073ffffffffffffffffffffffffffffffffffffffff8516600090815260208190526040812054145b8015610232575073ffffffffffffffffffffffffffffffffffffffff85166000908152602081905260409020600101548390105b61023b57610243565b3391506102e8565b6101966103ca60003973ffffffffffffffffffffffffffffffffffffffff3381166101965285166101b68190526000908152602081905260408120600201546101d6526101f68490526102169080f073ffffffffffffffffffffffffffffffffffffffff8616600090815260208190526040902060030180547fffffffffffffffffffffffff0000000000000000000000000000000000000000168217905591508190505b509392505050565b73ffffffffffffffffffffffffffffffffffffffff33166000908152602081905260408120548190821461032357610364565b60018054808201909155600090815260026020526040902080547fffffffffffffffffffffffff000000000000000000000000000000000000000016331790555b50503373ffffffffffffffffffffffffffffffffffffffff1660009081526020819052604090209081556001810192909255600290910155565b3373ffffffffffffffffffffffffffffffffffffffff166000908152602081905260409020600201555600608061019660043960048051602451604451606451600080547fffffffffffffffffffffffff0000000000000000000000000000000000000000908116909517815560018054909516909317909355600355915561013390819061006390396000f3007c0100000000000000000000000000000000000000000000000000000000600035046347810fe381146100445780637e4a1aa81461005557806383d2421b1461006957005b61004f6004356100ab565b60006000f35b6100636004356024356100fc565b60006000f35b61007460043561007a565b60006000f35b6001543373ffffffffffffffffffffffffffffffffffffffff9081169116146100a2576100a8565b60078190555b50565b73ffffffffffffffffffffffffffffffffffffffff8116600090815260026020526040902080547fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0016600117905550565b6001543373ffffffffffffffffffffffffffffffffffffffff9081169116146101245761012f565b600582905560068190555b505056");
        byte[] address = randomBytes(32);

        byte[] key_0 = Hex.decode("39a2338cbc13ff8523a9b1c9bc421b7518d63b70aa690ad37cb50908746c9a55");
        byte[] val_0 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000064");

        byte[] key_1 = Hex.decode("39a2338cbc13ff8523a9b1c9bc421b7518d63b70aa690ad37cb50908746c9a56");
        byte[] val_1 = Hex.decode("000000000000000000000000000000000000000000000000000000000000000c");

        byte[] key_2 = Hex.decode("4effac3ed62305246f40d058e1a9a8925a448d1967513482947d1d3f6104316f");
        byte[] val_2 = Hex.decode("7a65703300000000000000000000000000000000000000000000000000000000");

        byte[] key_3 = Hex.decode("4effac3ed62305246f40d058e1a9a8925a448d1967513482947d1d3f61043171");
        byte[] val_3 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000014");

        byte[] key_4 = Hex.decode("39a2338cbc13ff8523a9b1c9bc421b7518d63b70aa690ad37cb50908746c9a54");
        byte[] val_4 = Hex.decode("7a65703200000000000000000000000000000000000000000000000000000000");

        byte[] key_5 = Hex.decode("4effac3ed62305246f40d058e1a9a8925a448d1967513482947d1d3f61043170");
        byte[] val_5 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000078");

        byte[] key_6 = Hex.decode("e90b7bceb6e7df5418fb78d8ee546e97c83a08bbccc01a0644d599ccd2a7c2e0");
        byte[] val_6 = Hex.decode("00000000000000000000000010b426278fbec874791c4e3f9f48a59a44686efe");

        byte[] key_7 = Hex.decode("0df3cc3597c5ede0b1448e94daf1f1445aa541c6c03f602a426f04ae47508bb8");
        byte[] val_7 = Hex.decode("7a65703100000000000000000000000000000000000000000000000000000000");

        byte[] key_8 = Hex.decode("0df3cc3597c5ede0b1448e94daf1f1445aa541c6c03f602a426f04ae47508bb9");
        byte[] val_8 = Hex.decode("00000000000000000000000000000000000000000000000000000000000000c8");

        byte[] key_9 = Hex.decode("0df3cc3597c5ede0b1448e94daf1f1445aa541c6c03f602a426f04ae47508bba");
        byte[] val_9 = Hex.decode("000000000000000000000000000000000000000000000000000000000000000a");

        byte[] key_10 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000001");
        byte[] val_10 = Hex.decode("0000000000000000000000000000000000000000000000000000000000000003");

        byte[] key_11 = Hex.decode("0df3cc3597c5ede0b1448e94daf1f1445aa541c6c03f602a426f04ae47508bbb");
        byte[] val_11 = Hex.decode("0000000000000000000000007cd917d6194bcfc3670d8a1613e5b0c790036a35");

        byte[] key_12 = Hex.decode("679795a0195a1b76cdebb7c51d74e058aee92919b8c3389af86ef24535e8a28c");
        byte[] val_12 = Hex.decode("000000000000000000000000b0b0a72fcfe293a85bef5915e1a7acb37bf0c685");

        byte[] key_13 = Hex.decode("ac33ff75c19e70fe83507db0d683fd3465c996598dc972688b7ace676c89077b");
        byte[] val_13 = Hex.decode("0000000000000000000000000c6686f3d6ee27e285f2de7b68e8db25cf1b1063");


        ContractDetailsImpl contractDetails = new ContractDetailsImpl();
        contractDetails.setCode(code);
        contractDetails.setAddress(address);
        contractDetails.put(new DataWord(key_0), new DataWord(val_0));
        contractDetails.put(new DataWord(key_1), new DataWord(val_1));
        contractDetails.put(new DataWord(key_2), new DataWord(val_2));
        contractDetails.put(new DataWord(key_3), new DataWord(val_3));
        contractDetails.put(new DataWord(key_4), new DataWord(val_4));
        contractDetails.put(new DataWord(key_5), new DataWord(val_5));
        contractDetails.put(new DataWord(key_6), new DataWord(val_6));
        contractDetails.put(new DataWord(key_7), new DataWord(val_7));
        contractDetails.put(new DataWord(key_8), new DataWord(val_8));
        contractDetails.put(new DataWord(key_9), new DataWord(val_9));
        contractDetails.put(new DataWord(key_10), new DataWord(val_10));
        contractDetails.put(new DataWord(key_11), new DataWord(val_11));
        contractDetails.put(new DataWord(key_12), new DataWord(val_12));
        contractDetails.put(new DataWord(key_13), new DataWord(val_13));

        byte[] data = contractDetails.getEncoded();

        ContractDetailsImpl contractDetails_ = new ContractDetailsImpl(data);

        assertEquals(Hex.toHexString(code),
                Hex.toHexString(contractDetails_.getCode()));

        assertEquals(Hex.toHexString(address),
                Hex.toHexString(contractDetails_.getAddress()));

        assertEquals(Hex.toHexString(val_1),
                Hex.toHexString(contractDetails_.get(new DataWord(key_1)).getData()));

        assertEquals(Hex.toHexString(val_2),
                Hex.toHexString(contractDetails_.get(new DataWord(key_2)).getData()));

        assertEquals(Hex.toHexString(val_3),
                Hex.toHexString(contractDetails_.get(new DataWord(key_3)).getData()));

        assertEquals(Hex.toHexString(val_4),
                Hex.toHexString(contractDetails_.get(new DataWord(key_4)).getData()));

        assertEquals(Hex.toHexString(val_5),
                Hex.toHexString(contractDetails_.get(new DataWord(key_5)).getData()));

        assertEquals(Hex.toHexString(val_6),
                Hex.toHexString(contractDetails_.get(new DataWord(key_6)).getData()));

        assertEquals(Hex.toHexString(val_7),
                Hex.toHexString(contractDetails_.get(new DataWord(key_7)).getData()));

        assertEquals(Hex.toHexString(val_8),
                Hex.toHexString(contractDetails_.get(new DataWord(key_8)).getData()));

        assertEquals(Hex.toHexString(val_9),
                Hex.toHexString(contractDetails_.get(new DataWord(key_9)).getData()));

        assertEquals(Hex.toHexString(val_10),
                Hex.toHexString(contractDetails_.get(new DataWord(key_10)).getData()));

        assertEquals(Hex.toHexString(val_11),
                Hex.toHexString(contractDetails_.get(new DataWord(key_11)).getData()));

        assertEquals(Hex.toHexString(val_12),
                Hex.toHexString(contractDetails_.get(new DataWord(key_12)).getData()));

        assertEquals(Hex.toHexString(val_13),
                Hex.toHexString(contractDetails_.get(new DataWord(key_13)).getData()));
    }

    @Test
    public void testExternalStorageSerialization() {
        byte[] address = randomAddress();
        byte[] code = randomBytes(512);
        Map<DataWord, DataWord> elements = new HashMap<>();

        HashMapDB externalStorage = new HashMapDB();

        ContractDetailsImpl original = new ContractDetailsImpl();
        original.setExternalStorageDataSource(externalStorage);
        original.setAddress(address);
        original.setCode(code);
        original.externalStorage = true;

        for (int i = 0; i < IN_MEMORY_STORAGE_LIMIT / 64 + 10; i++) {
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
    }

    @Test
    public void testExternalStorageTransition() {
        byte[] address = randomAddress();
        byte[] code = randomBytes(512);
        Map<DataWord, DataWord> elements = new HashMap<>();

        HashMapDB externalStorage = new HashMapDB();

        ContractDetailsImpl original = new ContractDetailsImpl();
        original.setExternalStorageDataSource(externalStorage);
        original.setAddress(address);
        original.setCode(code);

        for (int i = 0; i < IN_MEMORY_STORAGE_LIMIT / 64 + 10; i++) {
            DataWord key = randomDataWord();
            DataWord value = randomDataWord();

            elements.put(key, value);
            original.put(key, value);
        }

        original.syncStorage();
        assertTrue(externalStorage.getAddedItems() == 0);

        ContractDetails deserialized = deserialize(original.getEncoded(), externalStorage);

        // adds keys for in-memory storage limit overflow
        for (int i = 0; i < 10; i++) {
            DataWord key = randomDataWord();
            DataWord value = randomDataWord();

            elements.put(key, value);
            deserialized.put(key, value);
        }
        
        deserialized.syncStorage();
        assertTrue(externalStorage.getAddedItems() > 0);

        deserialized = deserialize(deserialized.getEncoded(), externalStorage);

        Map<DataWord, DataWord> storage = deserialized.getStorage();
        assertEquals(elements.size(), storage.size());
        for (DataWord key : elements.keySet()) {
            assertEquals(elements.get(key), storage.get(key));
        }
    }

    private static ContractDetails deserialize(byte[] rlp, KeyValueDataSource externalStorage) {
        ContractDetailsImpl result = new ContractDetailsImpl();
        result.setExternalStorageDataSource(externalStorage);
        result.decode(rlp);
        
        return result;
    }
}
