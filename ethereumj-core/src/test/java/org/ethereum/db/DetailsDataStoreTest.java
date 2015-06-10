package org.ethereum.db;

import org.ethereum.datasource.HashMapDB;
import org.ethereum.vm.DataWord;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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


}
