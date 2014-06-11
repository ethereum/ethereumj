package org.ethereum.core;

import org.ethereum.vm.DataWord;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 09/06/2014 15:41
 */

public class ContractDetailsTest {

    @Test  /*  encode 2 keys/values  */
    public void test1(){

        String expected = "f888f842a00000000000000000000000000000000000000000000000000000000000000002a00000000000000000000000000000000000000000000000000000000000000001f842a00000000000000000000000000000000000000000000000000000000000009dd4a0000000000000000000000000000000000000000000000000000000000000765f";

        DataWord key1 = new DataWord(1);
        DataWord value1 = new DataWord(30303);

        DataWord key2 = new DataWord(2);
        DataWord value2 = new DataWord(40404);


        HashMap<DataWord, DataWord> storage = new HashMap<>();
        storage.put(key1, value1);
        storage.put(key2, value2);

        ContractDetails contractDetails = new ContractDetails(storage);

        String encoded = Hex.toHexString(contractDetails.getEncoded());

        Assert.assertEquals(expected, encoded);
    }

    @Test  /*  encode 3 keys/values  */
    public void test2(){

        String expected = "f8caf863a00000000000000000000000000000000000000000000000000000000000000002a00000000000000000000000000000000000000000000000000000000000000001a00000000000000000000000000000000000000000000000000000000000000003f863a00000000000000000000000000000000000000000000000000000000000009dd4a0000000000000000000000000000000000000000000000000000000000000765fa0000000000000000000000000000000000000000000000000000000000000ffff";

        DataWord key1 = new DataWord(1);
        DataWord value1 = new DataWord(30303);

        DataWord key2 = new DataWord(2);
        DataWord value2 = new DataWord(40404);

        DataWord key3 = new DataWord(3);
        DataWord value3 = new DataWord(0xFFFF);

        HashMap<DataWord, DataWord> storage = new HashMap<>();
        storage.put(key1, value1);
        storage.put(key2, value2);
        storage.put(key3, value3);

        ContractDetails contractDetails = new ContractDetails(storage);

        String encoded = Hex.toHexString(contractDetails.getEncoded());

        Assert.assertEquals(expected, encoded);
    }

    @Test  /*  decode 3 keys/values  */
    public void test3(){

        String rlpData = "f8caf863a00000000000000000000000000000000000000000000000000000000000000002a00000000000000000000000000000000000000000000000000000000000000001a00000000000000000000000000000000000000000000000000000000000000003f863a00000000000000000000000000000000000000000000000000000000000009dd4a0000000000000000000000000000000000000000000000000000000000000765fa0000000000000000000000000000000000000000000000000000000000000ffff";
        ContractDetails contractDetails = new ContractDetails(Hex.decode(rlpData));

        String expKey3String = "0000000000000000000000000000000000000000000000000000000000000003";
        String expVal3String = "000000000000000000000000000000000000000000000000000000000000ffff";

        DataWord key3   = contractDetails.storageKeys.get(2);
        DataWord value3 = contractDetails.storageValues.get(2);

        String key3String = Hex.toHexString(key3.getData());
        String value3String = Hex.toHexString(value3.getData());

        Assert.assertEquals(expKey3String, key3String);
        Assert.assertEquals(expVal3String, value3String);
    }


}
